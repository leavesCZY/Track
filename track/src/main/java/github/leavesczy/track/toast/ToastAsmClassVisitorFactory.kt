package github.leavesczy.track.toast

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.BaseTrackConfigParameters
import github.leavesczy.track.utils.replaceDotBySlash
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodInsnNode

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal abstract class ToastAsmClassVisitorFactory :
    BaseTrackAsmClassVisitorFactory<BaseTrackConfigParameters, ToastConfig> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): BaseTrackClassNode {
        return ToastClassVisitor(
            nextClassVisitor = nextClassVisitor,
            trackConfig = trackConfig
        )
    }

    override fun isTrackEnabled(classData: ClassData): Boolean {
        return classData.className != trackConfig.toasterClass
    }

}

private class ToastClassVisitor(
    private val nextClassVisitor: ClassVisitor,
    override val trackConfig: ToastConfig
) : BaseTrackClassNode(trackConfig = trackConfig) {

    private val toastClassDesc = "android/widget/Toast"

    private val showToastMethodName = "show"

    private val showToastMethodDesc = "()V"

    override fun visitEnd() {
        super.visitEnd()
        val toastMethodInsnNodeList = mutableListOf<MethodInsnNode>()
        methods.forEach { method ->
            method.instructions?.forEach {
                if (it is MethodInsnNode &&
                    it.opcode == Opcodes.INVOKEVIRTUAL &&
                    it.owner == toastClassDesc &&
                    it.name == showToastMethodName &&
                    it.desc == showToastMethodDesc
                ) {
                    toastMethodInsnNodeList.add(element = it)
                }
            }
        }
        if (toastMethodInsnNodeList.isNotEmpty()) {
            toastMethodInsnNodeList.forEach {
                it.opcode = Opcodes.INVOKESTATIC
                it.owner = replaceDotBySlash(className = trackConfig.toasterClass)
                it.name = trackConfig.showToastMethodName
                it.desc = Type.getMethodDescriptor(
                    Type.VOID_TYPE,
                    Type.getObjectType(toastClassDesc)
                )
                it.itf = false
            }
            nLog {
                name + " 发现 ${toastMethodInsnNodeList.size} 个 Toast.show 指令，完成处理..."
            }
        }
        accept(nextClassVisitor)
    }

}