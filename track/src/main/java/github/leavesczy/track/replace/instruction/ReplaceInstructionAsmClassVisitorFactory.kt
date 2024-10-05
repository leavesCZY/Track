package github.leavesczy.track.replace.instruction

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.BaseTrackConfigParameters
import github.leavesczy.track.utils.LogPrint
import github.leavesczy.track.utils.replacePeriodWithSlash
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal abstract class ReplaceInstructionAsmClassVisitorFactory :
    BaseTrackAsmClassVisitorFactory<BaseTrackConfigParameters, ReplaceInstructionConfig> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): BaseTrackClassNode {
        return ReplaceInstructionClassVisitor(
            nextClassVisitor = nextClassVisitor,
            trackConfig = trackConfig
        )
    }

    override fun isTrackEnabled(classData: ClassData): Boolean {
        return trackConfig.instructions.find { it.proxyOwner == classData.className } == null
    }

}

private class ReplaceInstructionClassVisitor(
    private val nextClassVisitor: ClassVisitor,
    override val trackConfig: ReplaceInstructionConfig
) : BaseTrackClassNode(trackConfig = trackConfig) {

    override fun visitEnd() {
        super.visitEnd()
        accept(nextClassVisitor)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        return ReplaceInstructionMethodVisitor(
            api = api,
            methodVisitor = methodVisitor,
            classNode = this,
            config = trackConfig
        )
    }

}

private class ReplaceInstructionMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor,
    private val classNode: ClassNode,
    private val config: ReplaceInstructionConfig
) : MethodVisitor(api, methodVisitor) {

    override fun visitFieldInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?
    ) {
        val find = config.instructions.find {
            it.owner == owner && it.name == name && (it.descriptor.isEmpty() || it.descriptor == descriptor)
        }
        if (find != null && opcode == Opcodes.GETSTATIC) {
            val proxyOwner = replacePeriodWithSlash(className = find.proxyOwner)
            super.visitFieldInsn(opcode, proxyOwner, name, descriptor)
            LogPrint.normal(tag = config.extensionName) {
                "${classNode.name} 发现符合规则的指令：$owner $name $descriptor , 替换为 $proxyOwner $name $descriptor ，完成处理..."
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
        val find = config.instructions.find {
            it.owner == owner && it.name == name && (it.descriptor.isEmpty() || it.descriptor == descriptor)
        }
        val mOpcode: Int
        val mOwner: String
        val mDescriptor: String
        if (find != null) {
            mOwner = replacePeriodWithSlash(className = find.proxyOwner)
            if (opcode == Opcodes.INVOKEVIRTUAL) {
                mOpcode = Opcodes.INVOKESTATIC
                mDescriptor = insetAsFirstArgument(descriptor = descriptor, owner = owner)
            } else {
                mOpcode = opcode
                mDescriptor = descriptor
            }
            LogPrint.normal(tag = config.extensionName) {
                "${classNode.name} 发现符合规则的指令：$owner $name $descriptor , 替换为 $mOwner $name $mDescriptor ，完成处理..."
            }
        } else {
            mOpcode = opcode
            mOwner = owner
            mDescriptor = descriptor
        }
        super.visitMethodInsn(mOpcode, mOwner, name, mDescriptor, isInterface)
    }

    private fun insetAsFirstArgument(descriptor: String, owner: String): String {
        val argumentsDescriptor = descriptor.substringAfter('(').substringBefore(')')
        val arguments = argumentsDescriptor.split(";").toMutableList().apply {
            add(0, "L${owner}")
        }.joinToString(separator = ";")
        return "(${arguments})" + descriptor.substringAfter(')')
    }

}