package github.leavesczy.track.click.compose

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.BaseTrackConfigParameters
import github.leavesczy.track.utils.InitMethodName
import github.leavesczy.track.utils.replacePeriodWithSlash
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
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
 * @Date: 2025/5/16 11:41
 * @Desc:
 */
private const val ClickableElementClassName = "androidx.compose.foundation.ClickableElement"

private const val CombinedClickableElementClassName =
    "androidx.compose.foundation.CombinedClickableElement"

internal abstract class ComposeClickAsmClassVisitorFactory2 :
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
        return classData.className == ClickableElementClassName || classData.className == CombinedClickableElementClassName
    }

}

private class ComposeClickClassVisitor(
    private val nextClassVisitor: ClassVisitor,
    override val trackConfig: ComposeClickConfig
) : BaseTrackClassNode(trackConfig = trackConfig) {

    override fun visitEnd() {
        super.visitEnd()
        log {
            "找到 $ClickableElementClassName , $CombinedClickableElementClassName 类，完成处理..."
        }
        accept(nextClassVisitor)
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
        if (name == InitMethodName) {
            handleComposeClick(methodNode = methodNode)
        }
        return methodNode
    }

    private fun handleComposeClick(methodNode: MethodNode) {
        if (methodNode.signature.isNullOrBlank()) {
            return
        }
        val onClickLabelType = Type.getType("Ljava/lang/String;")
        val onClickFunctionType = Type.getType("Lkotlin/jvm/functions/Function0;")
        val methodDesc = methodNode.desc
        val methodArgumentTypes = Type.getArgumentTypes(methodDesc)
        val onClickLabelArgumentIndex = methodArgumentTypes.indexOf(element = onClickLabelType) + 1
        val onClickArgumentIndex = methodArgumentTypes.indexOf(element = onClickFunctionType) + 1
        insertInstructions(
            methodNode = methodNode,
            onClickLabelArgumentIndex = onClickLabelArgumentIndex,
            onClickArgumentIndex = onClickArgumentIndex
        )
    }

    private fun insertInstructions(
        methodNode: MethodNode,
        onClickArgumentIndex: Int,
        onClickLabelArgumentIndex: Int
    ) {
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
        val onClickClassFormat = replacePeriodWithSlash(className = trackConfig.onClickClass)
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