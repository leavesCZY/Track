package github.leavesczy.track.toast

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import github.leavesczy.track.utils.LogPrint
import github.leavesczy.track.utils.replaceDotBySlash
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal abstract class ToastClassVisitorFactory :
    AsmClassVisitorFactory<ToastClassVisitorFactory.ToastConfigParameters> {

    interface ToastConfigParameters : InstrumentationParameters {
        @get:Input
        val config: Property<ToastConfig>
    }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return ToastClassVisitor(
            config = parameters.get().config.get(),
            nextClassVisitor = nextClassVisitor
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val config = parameters.get().config.get()
        if (classData.className == config.toasterClass) {
            return false
        }
        return true
    }

}

private class ToastClassVisitor(
    private val config: ToastConfig,
    private val nextClassVisitor: ClassVisitor
) : ClassNode(Opcodes.ASM5) {

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