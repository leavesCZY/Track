package github.leavesczy.trace.click

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import github.leavesczy.trace.utils.InitMethodName
import github.leavesczy.trace.utils.replaceDotBySlash
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal interface ComposeClickConfigParameters : InstrumentationParameters {
    @get:Input
    val config: Property<ComposeClickConfig>
}

internal abstract class ComposeClickClassVisitorFactory :
    AsmClassVisitorFactory<ComposeClickConfigParameters> {

    private companion object {

        private const val ComposeClickClassName = "androidx.compose.foundation.ClickableKt"

    }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return ComposeClickClassVisitor(
            nextClassVisitor = nextClassVisitor,
            config = parameters.get().config.get()
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        return classData.className == ComposeClickClassName
    }

}

private class ComposeClickClassVisitor(
    private val nextClassVisitor: ClassVisitor,
    private val config: ComposeClickConfig,
) : ClassNode(Opcodes.ASM5) {

    private companion object {

        private const val ClickableMethodDesc =
            "(Landroidx/compose/ui/Modifier;Landroidx/compose/foundation/interaction/MutableInteractionSource;Landroidx/compose/foundation/Indication;ZLjava/lang/String;Landroidx/compose/ui/semantics/Role;Lkotlin/jvm/functions/Function0;)Landroidx/compose/ui/Modifier;"

        private const val CombinedClickableMethodDesc =
            "(Landroidx/compose/ui/Modifier;Landroidx/compose/foundation/interaction/MutableInteractionSource;Landroidx/compose/foundation/Indication;ZLjava/lang/String;Landroidx/compose/ui/semantics/Role;Ljava/lang/String;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;)Landroidx/compose/ui/Modifier;"

    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val methodNode =
            super.visitMethod(access, name, descriptor, signature, exceptions) as MethodNode
        hookComposeClick(methodNode = methodNode)
        return methodNode
    }

    override fun visitEnd() {
        super.visitEnd()
        accept(nextClassVisitor)
    }

    private fun hookComposeClick(methodNode: MethodNode) {
        val onClickArgumentIndex = when (methodNode.desc) {
            ClickableMethodDesc -> {
                6
            }

            CombinedClickableMethodDesc -> {
                9
            }

            else -> {
                -1
            }
        }
        if (onClickArgumentIndex > 0) {
            val onClickLabelArgumentIndex = 4
            val input = InsnList()
            input.add(LdcInsnNode(config.onClickWhiteList))
            input.add(VarInsnNode(Opcodes.ALOAD, onClickLabelArgumentIndex))
            input.add(
                MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/String",
                    "equals",
                    "(Ljava/lang/Object;)Z",
                    false
                )
            )
            val onClickClassFormat = replaceDotBySlash(className = config.onClickClass)
            val label = LabelNode()
            input.add(JumpInsnNode(Opcodes.IFNE, label))
            input.add(TypeInsnNode(Opcodes.NEW, onClickClassFormat))
            input.add(InsnNode(Opcodes.DUP))
            input.add(VarInsnNode(Opcodes.ALOAD, onClickArgumentIndex))
            input.add(
                MethodInsnNode(
                    Opcodes.INVOKESPECIAL,
                    onClickClassFormat,
                    InitMethodName,
                    "(Lkotlin/jvm/functions/Function0;)V",
                    false
                )
            )
            input.add(VarInsnNode(Opcodes.ASTORE, onClickArgumentIndex))
            input.add(label)
            methodNode.instructions.insert(input)
        }
    }

}