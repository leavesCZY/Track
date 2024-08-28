package github.leavesczy.track.thread

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.BaseTrackConfigParameters
import github.leavesczy.track.utils.LogPrint
import github.leavesczy.track.utils.insertArgument
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal abstract class OptimizedThreadAsmClassVisitorFactory :
    BaseTrackAsmClassVisitorFactory<BaseTrackConfigParameters, OptimizedThreadConfig> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): BaseTrackClassNode {
        return OptimizedThreadClassVisitor(
            config = trackConfig,
            nextClassVisitor = nextClassVisitor
        )
    }

    override fun isTrackEnabled(classData: ClassData): Boolean {
        return true
    }

}

private class OptimizedThreadClassVisitor(
    private val config: OptimizedThreadConfig,
    private val nextClassVisitor: ClassVisitor
) : BaseTrackClassNode() {

    private val executorsClass = "java/util/concurrent/Executors"

    override fun visitEnd() {
        super.visitEnd()
        methods.forEach { methodNode ->
            methodNode.instructions?.forEach { instruction ->
                val opcode = instruction.opcode
                if (opcode == Opcodes.INVOKESTATIC) {
                    val methodInsnNode = instruction as? MethodInsnNode
                    if (methodInsnNode?.owner == executorsClass) {
                        transformInvokeExecutorsInstruction(
                            methodNode,
                            instruction
                        )
                    }
                }
            }
        }
        accept(nextClassVisitor)
    }

    private fun transformInvokeExecutorsInstruction(
        methodNode: MethodNode,
        methodInsnNode: MethodInsnNode
    ) {
        val pointMethod = config.executorsMethodNames.find { it == methodInsnNode.name }
        if (pointMethod != null) {
            methodInsnNode.owner = config.optimizedExecutorsClass
            methodInsnNode.insertArgument(String::class.java)
            val mClassSimpleName = name.substringAfterLast('/')
            methodNode.instructions.insertBefore(methodInsnNode, LdcInsnNode(mClassSimpleName))
            LogPrint.normal(tag = "OptimizedThreadTrack") {
                "在 $mClassSimpleName 中找到 ${methodInsnNode.name} 方法，完成处理..."
            }
        }
    }

}