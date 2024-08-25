package github.leavesczy.track.thread

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.BaseTrackClassVisitorFactory
import github.leavesczy.track.BaseTrackConfigParameters
import github.leavesczy.track.utils.LogPrint
import github.leavesczy.track.utils.insertArgument
import github.leavesczy.track.utils.simpleClassName
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
internal abstract class OptimizedThreadClassVisitorFactory :
    BaseTrackClassVisitorFactory<BaseTrackConfigParameters, OptimizedThreadConfig> {

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
            //将 Executors 替换为 OptimizedThreadPool
            methodInsnNode.owner = config.optimizedExecutorsClass
            //为调用 newFixedThreadPool 等方法的指令多插入一个 String 类型的方法入参参数声明
            methodInsnNode.insertArgument(String::class.java)
            //将 className 作为上述 String 参数的入参参数
            methodNode.instructions.insertBefore(methodInsnNode, LdcInsnNode(simpleClassName))
            LogPrint.normal(tag = "OptimizedThreadTrack") {
                "在 $simpleClassName 中找到 ${methodInsnNode.name} 方法，完成替换..."
            }
        }
    }

}