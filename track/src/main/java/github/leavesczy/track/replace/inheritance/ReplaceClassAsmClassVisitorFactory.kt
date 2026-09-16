package github.leavesczy.track.replace.inheritance

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.utils.AsmApi
import github.leavesczy.track.utils.replacePeriodWithSlash
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

internal abstract class ReplaceClassAsmClassVisitorFactory :
    BaseTrackAsmClassVisitorFactory<ReplaceClassConfigParameters, ReplaceClassConfig> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): BaseTrackClassNode {
        return ReplaceClassVisitor(
            nextClassVisitor = nextClassVisitor,
            trackConfig = trackConfig
        )
    }

    override fun isTrackEnabled(classData: ClassData): Boolean {
        val superClasses = classData.superClasses
        if (classData.className == trackConfig.targetClass || superClasses.isEmpty()) {
            return false
        }
        return superClasses.first() == trackConfig.originClass
    }

}

private class ReplaceClassVisitor(
    private val nextClassVisitor: ClassVisitor,
    override val trackConfig: ReplaceClassConfig
) : BaseTrackClassNode(trackConfig = trackConfig) {

    private val oldSuperName = replacePeriodWithSlash(className = trackConfig.originClass)

    private val newSuperName = replacePeriodWithSlash(className = trackConfig.targetClass)

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(
            version,
            access,
            name,
            replaceSuperTypeInSignature(signature = signature),
            newSuperName,
            interfaces
        )
        log {
            "$name 的父类符合规则，完成处理..."
        }
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        return object : MethodVisitor(AsmApi, methodVisitor) {
            override fun visitMethodInsn(
                opcode: Int,
                owner: String?,
                methodName: String?,
                methodDescriptor: String?,
                isInterface: Boolean
            ) {
                // 编译器生成的 super.xxx() / super(...) 都是 INVOKESPECIAL，且 owner 为直接父类。
                // 仅改写 <init> 会漏掉业务方法上的 super 调用，从而绕过新父类覆写。
                val currentOwner = if (opcode == Opcodes.INVOKESPECIAL && owner == oldSuperName) {
                    newSuperName
                } else {
                    owner
                }
                super.visitMethodInsn(
                    opcode,
                    currentOwner,
                    methodName,
                    methodDescriptor,
                    isInterface
                )
            }
        }
    }

    override fun visitEnd() {
        super.visitEnd()
        accept(nextClassVisitor)
    }

    private fun replaceSuperTypeInSignature(signature: String?): String? {
        if (signature.isNullOrEmpty()) {
            return signature
        }
        val oldType = "L$oldSuperName;"
        val newType = "L$newSuperName;"
        if (!signature.contains(oldType)) {
            return signature
        }
        return signature.replace(oldType, newType)
    }

}
