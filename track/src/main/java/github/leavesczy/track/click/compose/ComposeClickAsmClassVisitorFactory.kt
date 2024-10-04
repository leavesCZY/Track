package github.leavesczy.track.click.compose

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.BaseTrackConfigParameters
import github.leavesczy.track.utils.InitMethodName
import github.leavesczy.track.utils.replaceDotBySlash
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
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
private const val ComposeClickableClassName = "androidx.compose.foundation.ClickableKt"

internal abstract class ComposeClickAsmClassVisitorFactory :
    BaseTrackAsmClassVisitorFactory<BaseTrackConfigParameters, ComposeClickConfig> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): BaseTrackClassNode {
        return ComposeClickClassVisitor(
            nextClassVisitor = nextClassVisitor,
            trackConfig = trackConfig
        )
    }

    override fun isTrackEnabled(classData: ClassData): Boolean {
        return classData.className == ComposeClickableClassName
    }

}

private class ComposeClickClassVisitor(
    private val nextClassVisitor: ClassVisitor,
    override val trackConfig: ComposeClickConfig
) : BaseTrackClassNode(trackConfig = trackConfig) {

    private val clickableMethodDesc =
        "(Landroidx/compose/ui/Modifier;Landroidx/compose/foundation/interaction/MutableInteractionSource;Landroidx/compose/foundation/Indication;ZLjava/lang/String;Landroidx/compose/ui/semantics/Role;Lkotlin/jvm/functions/Function0;)Landroidx/compose/ui/Modifier;"

    private val combinedClickableMethodDesc =
        "(Landroidx/compose/ui/Modifier;Landroidx/compose/foundation/interaction/MutableInteractionSource;Landroidx/compose/foundation/Indication;ZLjava/lang/String;Landroidx/compose/ui/semantics/Role;Ljava/lang/String;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;)Landroidx/compose/ui/Modifier;"

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val methodNode =
            super.visitMethod(access, name, descriptor, signature, exceptions) as MethodNode
        handleComposeClick(methodNode = methodNode)
        return methodNode
    }

    override fun visitEnd() {
        super.visitEnd()
        log {
            "找到 $ComposeClickableClassName 类，完成处理..."
        }
        accept(nextClassVisitor)
    }

    private fun handleComposeClick(methodNode: MethodNode) {
        val onClickArgumentIndex = when (methodNode.desc) {
            clickableMethodDesc -> {
                6
            }

            combinedClickableMethodDesc -> {
                9
            }

            else -> {
                -1
            }
        }
        if (onClickArgumentIndex > 0) {
            val onClickLabelArgumentIndex = 4
            val input = InsnList()
            input.add(LdcInsnNode(trackConfig.onClickWhiteList))
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
            val onClickClassFormat = replaceDotBySlash(className = trackConfig.onClickClass)
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