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
        val find = config.replacements.find {
            it.kind == MemberKind.FIELD &&
                    it.ownerClass == owner &&
                    it.memberName == name &&
                    matchesDescriptor(ruleDescriptor = it.descriptor, actualDescriptor = descriptor)
        }
        if (find != null && opcode == Opcodes.GETSTATIC) {
            val proxyClass = replacePeriodWithSlash(className = find.proxyClass)
            super.visitFieldInsn(opcode, proxyClass, name, descriptor)
            LogPrint.normal(tag = "memberTrack") {
                "$className 发现符合规则的指令：$owner $name $descriptor , 替换为 $proxyClass $name $descriptor ，完成处理..."
            }
        } else {
            super.visitFieldInsn(opcode, owner, name, descriptor)
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
