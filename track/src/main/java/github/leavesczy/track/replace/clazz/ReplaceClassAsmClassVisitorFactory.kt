package github.leavesczy.track.replace.clazz

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.BaseTrackConfigParameters
import github.leavesczy.track.utils.ApiOpcodes
import github.leavesczy.track.utils.InitMethodName
import github.leavesczy.track.utils.replacePeriodWithSlash
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @Author: leavesCZY
 * @Date: 2025/5/16 11:42
 * @Desc:
 */
internal abstract class ReplaceClassAsmClassVisitorFactory :
    BaseTrackAsmClassVisitorFactory<BaseTrackConfigParameters, ReplaceClassConfig> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): BaseTrackClassNode {
        return ReplaceClassClassVisitor(
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

private class ReplaceClassClassVisitor(
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
            signature,
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
        return object : MethodVisitor(ApiOpcodes, methodVisitor) {
            override fun visitMethodInsn(
                opcode: Int,
                owner: String?,
                methodName: String?,
                methodDescriptor: String?,
                isInterface: Boolean
            ) {
                val currentOwner = if (opcode == Opcodes.INVOKESPECIAL &&
                    methodName == InitMethodName &&
                    owner == oldSuperName
                ) {
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

}