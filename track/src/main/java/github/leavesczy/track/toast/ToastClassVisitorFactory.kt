package github.leavesczy.track.toast

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.BaseTrackClassVisitorFactory
import github.leavesczy.track.BaseTrackConfigParameters
import github.leavesczy.track.utils.LogPrint
import github.leavesczy.track.utils.replaceDotBySlash
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal abstract class ToastClassVisitorFactory :
    BaseTrackClassVisitorFactory<BaseTrackConfigParameters, ToastConfig> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): BaseTrackClassNode {
        return ToastClassVisitor(
            config = trackConfig,
            nextClassVisitor = nextClassVisitor
        )
    }

    override fun isTrackEnabled(classData: ClassData): Boolean {
        return classData.className != trackConfig.toasterClass
    }

}

private class ToastClassVisitor(
    private val config: ToastConfig,
    private val nextClassVisitor: ClassVisitor
) : BaseTrackClassNode() {

    private val toastClass = "android/widget/Toast"

    override fun visitEnd() {
        super.visitEnd()
        val toastMethodInsnNodeList = mutableListOf<MethodInsnNode>()
        methods.forEach { method ->
            method.instructions?.forEach {
                if (it is MethodInsnNode && it.opcode == Opcodes.INVOKEVIRTUAL && it.owner == toastClass && it.name == "show" && it.desc == "()V") {
                    toastMethodInsnNodeList.add(element = it)
                }
            }
        }
        if (toastMethodInsnNodeList.isNotEmpty()) {
            toastMethodInsnNodeList.forEach {
                it.opcode = Opcodes.INVOKESTATIC
                it.owner = replaceDotBySlash(className = config.toasterClass)
                it.name = config.showToastMethodName
                it.desc = "(L$toastClass;)V"
                it.itf = false
            }
            LogPrint.normal(tag = "ToastTrack") {
                name + " 发现 ${toastMethodInsnNodeList.size} 个 Toast.show 指令，完成处理..."
            }
        }
        accept(nextClassVisitor)
    }

}