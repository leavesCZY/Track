package github.leavesczy.track.member

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.utils.ASM_API
import github.leavesczy.track.utils.LogPrint
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

    /** 跳过 proxy 类，避免代理实现再被改写造成递归。 */
    override fun isTrackEnabled(classData: ClassData): Boolean {
        return classData.className !in trackConfig.proxyClasses
    }

}

private class MemberClassVisitor(
    nextClassVisitor: ClassVisitor,
    trackConfig: MemberConfig
) : ClassVisitor(ASM_API, nextClassVisitor) {

    private val fieldIndex = indexReplacements(
        replacements = trackConfig.replacements,
        kind = MemberKind.FIELD
    )

    private val methodIndex = indexReplacements(
        replacements = trackConfig.replacements,
        kind = MemberKind.METHOD
    )

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
            fieldIndex = fieldIndex,
            methodIndex = methodIndex
        )
    }

}

private class MemberMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor,
    private val className: String,
    private val fieldIndex: Map<String, List<MemberConfig.MemberReplacement>>,
    private val methodIndex: Map<String, List<MemberConfig.MemberReplacement>>
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
        val find = findReplacement(
            index = fieldIndex,
            owner = owner,
            name = name,
            descriptor = descriptor
        )
        if (find == null) {
            super.visitFieldInsn(opcode, owner, name, descriptor)
            return
        }
        val proxyOwner = find.proxyOwner
        when (opcode) {
            Opcodes.GETSTATIC -> {
                super.visitFieldInsn(opcode, proxyOwner, name, descriptor)
                LogPrint.normal(tag = "memberTrack") {
                    "$className 发现符合规则的指令：GETSTATIC $owner $name $descriptor , 替换为 GETSTATIC $proxyOwner $name $descriptor ，完成处理..."
                }
            }
            Opcodes.GETFIELD -> {
                val methodDescriptor = Type.getMethodDescriptor(
                    Type.getType(descriptor),
                    Type.getObjectType(owner)
                )
                super.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    proxyOwner,
                    name,
                    methodDescriptor,
                    false
                )
                LogPrint.normal(tag = "memberTrack") {
                    "$className 发现符合规则的指令：GETFIELD $owner $name $descriptor , 替换为 INVOKESTATIC $proxyOwner $name $methodDescriptor ，完成处理..."
                }
            }
            else -> {
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
        val find = findReplacement(
            index = methodIndex,
            owner = owner,
            name = name,
            descriptor = descriptor
        )
        if (find == null) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            return
        }
        if (opcode == Opcodes.INVOKESPECIAL) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            return
        }
        val resultOwner = find.proxyOwner
        val resultOpcode: Int
        val resultDescriptor: String
        val resultIsInterface: Boolean
        if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEINTERFACE) {
            resultOpcode = Opcodes.INVOKESTATIC
            resultDescriptor = insertAsFirstArgument(descriptor = descriptor, owner = owner)
            resultIsInterface = false
        } else {
            resultOpcode = opcode
            resultDescriptor = descriptor
            resultIsInterface = isInterface
        }
        LogPrint.normal(tag = "memberTrack") {
            "$className 发现符合规则的指令：$owner $name $descriptor , 替换为 $resultOwner $name $resultDescriptor ，完成处理..."
        }
        super.visitMethodInsn(resultOpcode, resultOwner, name, resultDescriptor, resultIsInterface)
    }

    private fun findReplacement(
        index: Map<String, List<MemberConfig.MemberReplacement>>,
        owner: String,
        name: String,
        descriptor: String
    ): MemberConfig.MemberReplacement? {
        val candidates = index[memberKey(owner = owner, name = name)] ?: return null
        return candidates.find { replacement ->
            matchesDescriptor(ruleDescriptor = replacement.descriptor, actualDescriptor = descriptor)
        }
    }

    private fun matchesDescriptor(ruleDescriptor: String, actualDescriptor: String): Boolean {
        return ruleDescriptor == MATCH_ALL_DESCRIPTORS || ruleDescriptor == actualDescriptor
    }

    private fun insertAsFirstArgument(descriptor: String, owner: String): String {
        val returnType = Type.getReturnType(descriptor)
        val argumentTypes = Type.getArgumentTypes(descriptor)
        val newArgumentTypes = arrayOf(Type.getObjectType(owner), *argumentTypes)
        return Type.getMethodDescriptor(returnType, *newArgumentTypes)
    }

}

private fun indexReplacements(
    replacements: Set<MemberConfig.MemberReplacement>,
    kind: MemberKind
): Map<String, List<MemberConfig.MemberReplacement>> {
    return replacements.filter { it.kind == kind }.groupBy { replacement ->
        memberKey(owner = replacement.ownerClass, name = replacement.memberName)
    }
}

private fun memberKey(owner: String, name: String): String {
    return "$owner#$name"
}
