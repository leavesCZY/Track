package github.leavesczy.trace.toast

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import github.leavesczy.trace.utils.replaceDotBySlash
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal interface ToastConfigParameters : InstrumentationParameters {
    @get:Input
    val config: Property<ToastConfig>
}

internal abstract class ToastClassVisitorFactory :
    AsmClassVisitorFactory<ToastConfigParameters> {

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

    companion object {

        private const val TOAST = "android/widget/Toast"

    }

    override fun visitEnd() {
        super.visitEnd()
        methods.forEach {
            hook(methodNode = it)
        }
        accept(nextClassVisitor)
    }

    private fun hook(methodNode: MethodNode) {
        val instructions = methodNode.instructions
        if (instructions != null && instructions.size() > 0) {
            instructions.mapNotNull {
                if (it is MethodInsnNode && it.opcode == Opcodes.INVOKEVIRTUAL && it.owner == TOAST && it.name == "show" && it.desc == "()V") {
                    it
                } else {
                    null
                }
            }.forEach {
                it.opcode = Opcodes.INVOKESTATIC
                it.owner = replaceDotBySlash(className = config.toasterClass)
                it.name = config.showToastMethodName
                it.desc = "(L$TOAST;)V"
                it.itf = false
            }
        }
    }

}