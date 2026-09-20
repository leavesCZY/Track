package github.leavesczy.track.member

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.utils.ASM_API
import github.leavesczy.track.utils.LogPrint
import github.leavesczy.track.utils.replacePeriodWithSlash
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 * 成员替换：在调用点改写字段读 / 方法调用的 owner（及必要时的调用约定）。
 *
 * - GETSTATIC：只换 owner，proxy 提供同名同类型字段
 * - GETFIELD：改为 INVOKESTATIC proxy.name(receiver)
 * - INVOKESTATIC：只换 owner
 * - INVOKEVIRTUAL / INVOKEINTERFACE：改为 INVOKESTATIC，并把 receiver 插入为第一参数
 * - INVOKESPECIAL、PUT*：不改写
 */
internal abstract class MemberAsmClassVisitorFactory :
    BaseTrackAsmClassVisitorFactory<MemberConfigParameters, MemberConfig> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return MemberClassVisitor(
            nextClassVisitor = nextClassVisitor,
            trackConfig = trackConfig
        )
    }

    /** 跳过 proxy 自身，避免 ToastProxy.show → Toast.show 再被改回造成递归。 */
    override fun isTrackEnabled(classData: ClassData): Boolean {
        return trackConfig.replacements.find { it.proxyClass == classData.className } == null
    }

}

private class MemberClassVisitor(
    nextClassVisitor: ClassVisitor,
    private val trackConfig: MemberConfig
) : ClassVisitor(ASM_API, nextClassVisitor) {

    private var className = ""

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        className = name.orEmpty()
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        return MemberMethodVisitor(
            api = api,
            methodVisitor = methodVisitor,
            className = className,
            config = trackConfig
        )
    }

}

private class MemberMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor,
    private val className: String,
    private val config: MemberConfig
) : MethodVisitor(api, methodVisitor) {

    override fun visitFieldInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?
    ) {
        if (owner == null || name == null || descriptor == null) {
            super.visitFieldInsn(opcode, owner, name, descriptor)
            return
        }
        val find = config.replacements.find {
            it.kind == MemberKind.FIELD &&
                    it.ownerClass == owner &&
                    it.memberName == name &&
                    matchesDescriptor(ruleDescriptor = it.descriptor, actualDescriptor = descriptor)
        }
        if (find == null) {
            super.visitFieldInsn(opcode, owner, name, descriptor)
            return
        }
        val proxyClass = replacePeriodWithSlash(className = find.proxyClass)
        when (opcode) {
            Opcodes.GETSTATIC -> {
                // 静态字段：只换 owner，proxy 需提供同名同类型字段（如 @JvmField）。
                super.visitFieldInsn(opcode, proxyClass, name, descriptor)
                LogPrint.normal(tag = "memberTrack") {
                    "$className 发现符合规则的指令：GETSTATIC $owner $name $descriptor , 替换为 GETSTATIC $proxyClass $name $descriptor ，完成处理..."
                }
            }
            Opcodes.GETFIELD -> {
                // 实例字段：改为静态方法，receiver 作为首参，proxy 需
                // @JvmStatic fun fieldName(owner: Owner): FieldType
                val methodDescriptor = Type.getMethodDescriptor(
                    Type.getType(descriptor),
                    Type.getObjectType(owner)
                )
                super.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    proxyClass,
                    name,
                    methodDescriptor,
                    false
                )
                LogPrint.normal(tag = "memberTrack") {
                    "$className 发现符合规则的指令：GETFIELD $owner $name $descriptor , 替换为 INVOKESTATIC $proxyClass $name $methodDescriptor ，完成处理..."
                }
            }
            else -> {
                // PUTSTATIC / PUTFIELD 暂不改写。
                super.visitFieldInsn(opcode, owner, name, descriptor)
            }
        }
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        val find = config.replacements.find {
            it.kind == MemberKind.METHOD &&
                    it.ownerClass == owner &&
                    it.memberName == name &&
                    matchesDescriptor(ruleDescriptor = it.descriptor, actualDescriptor = descriptor)
        }
        if (find == null) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            return
        }
        // INVOKESPECIAL（private / super / <init>）改 owner 会导致非法字节码，直接跳过。
        if (opcode == Opcodes.INVOKESPECIAL) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            return
        }
        val resultOwner = replacePeriodWithSlash(className = find.proxyClass)
        val resultOpcode: Int
        val resultDescriptor: String
        val resultIsInterface: Boolean
        if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEINTERFACE) {
            // 实例/接口调用 → 静态代理，descriptor 前面插入原 owner 类型作为 receiver。
            resultOpcode = Opcodes.INVOKESTATIC
            resultDescriptor = insertAsFirstArgument(descriptor = descriptor, owner = owner)
            resultIsInterface = false
        } else {
            // INVOKESTATIC：保持静态调用，仅替换 owner。
            resultOpcode = opcode
            resultDescriptor = descriptor
            resultIsInterface = isInterface
        }
        LogPrint.normal(tag = "memberTrack") {
            "$className 发现符合规则的指令：$owner $name $descriptor , 替换为 $resultOwner $name $resultDescriptor ，完成处理..."
        }
        super.visitMethodInsn(resultOpcode, resultOwner, name, resultDescriptor, resultIsInterface)
    }

    private fun matchesDescriptor(ruleDescriptor: String, actualDescriptor: String?): Boolean {
        return ruleDescriptor == MATCH_ALL_DESCRIPTORS || ruleDescriptor == actualDescriptor
    }

    private fun insertAsFirstArgument(descriptor: String, owner: String): String {
        val returnType = Type.getReturnType(descriptor)
        val argumentTypes = Type.getArgumentTypes(descriptor)
        val newArgumentTypes = arrayOf(Type.getObjectType(owner), *argumentTypes)
        return Type.getMethodDescriptor(returnType, *newArgumentTypes)
    }

}
