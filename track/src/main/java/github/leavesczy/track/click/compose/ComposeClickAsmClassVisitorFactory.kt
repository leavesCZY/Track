package github.leavesczy.track.click.compose

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.utils.InitMethodName
import github.leavesczy.track.utils.replacePeriodWithSlash
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

private const val CLICKABLE_ELEMENT_CLASS_NAME = "androidx.compose.foundation.ClickableElement"

private const val COMBINED_CLICKABLE_ELEMENT_CLASS_NAME =
    "androidx.compose.foundation.CombinedClickableElement"

private const val ON_CLICK_LABEL_PARAM_NAME = "onClickLabel"

private const val ON_CLICK_PARAM_NAME = "onClick"

private const val STRING_DESC = "Ljava/lang/String;"

private const val FUNCTION0_DESC = "Lkotlin/jvm/functions/Function0;"

private const val DEFAULT_CONSTRUCTOR_MARKER_CLASS_NAME =
    "kotlin.jvm.internal.DefaultConstructorMarker"

internal abstract class ComposeClickAsmClassVisitorFactory :
    BaseTrackAsmClassVisitorFactory<ComposeClickConfigParameters, ComposeClickConfig> {

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
        return classData.className == CLICKABLE_ELEMENT_CLASS_NAME ||
                classData.className == COMBINED_CLICKABLE_ELEMENT_CLASS_NAME
    }

}

private class ComposeClickClassVisitor(
    private val nextClassVisitor: ClassVisitor,
    override val trackConfig: ComposeClickConfig
) : BaseTrackClassNode(trackConfig = trackConfig) {

    override fun visitEnd() {
        super.visitEnd()
        val primaryConstructors = methods.filter { methodNode ->
            methodNode.name == InitMethodName && methodNode.isPrimaryClickableConstructor()
        }
        if (primaryConstructors.isEmpty()) {
            throw composeClickTrackError(
                detail = "未找到会 putfield onClick 的主构造方法，拒绝继续编译"
            )
        }
        if (primaryConstructors.size > 1) {
            throw composeClickTrackError(
                detail = "找到多个主构造方法，拒绝重复插桩：${primaryConstructors.map { it.desc }}"
            )
        }
        handleComposeClick(methodNode = primaryConstructors[0])
        log {
            "找到 $name 类，完成处理..."
        }
        accept(nextClassVisitor)
    }

    /**
     * ClickableKt 有多组 clickable / combinedClickable 重载，最终都会走到
     * ClickableElement / CombinedClickableElement 的主构造。
     * 只改「真正 putfield onClick」的主构造，跳过带 DefaultConstructorMarker 的 synthetic 转发构造，
     * 避免同一点击被重复包装。
     */
    private fun MethodNode.isPrimaryClickableConstructor(): Boolean {
        val argumentTypes = Type.getArgumentTypes(desc)
        if (argumentTypes.isNotEmpty() &&
            argumentTypes.last().className == DEFAULT_CONSTRUCTOR_MARKER_CLASS_NAME
        ) {
            return false
        }
        val classInternalName = this@ComposeClickClassVisitor.name
        return instructions.any { insn ->
            insn is FieldInsnNode &&
                    insn.opcode == Opcodes.PUTFIELD &&
                    insn.owner == classInternalName &&
                    insn.name == ON_CLICK_PARAM_NAME &&
                    insn.desc == FUNCTION0_DESC
        }
    }

    private fun handleComposeClick(methodNode: MethodNode) {
        val onClickLabelSlot = methodNode.findRequiredParamSlot(
            paramName = ON_CLICK_LABEL_PARAM_NAME,
            expectedDesc = STRING_DESC
        )
        val onClickSlot = methodNode.findRequiredParamSlot(
            paramName = ON_CLICK_PARAM_NAME,
            expectedDesc = FUNCTION0_DESC
        )
        insertInstructions(
            methodNode = methodNode,
            onClickLabelSlot = onClickLabelSlot,
            onClickSlot = onClickSlot
        )
    }

    private fun MethodNode.findRequiredParamSlot(paramName: String, expectedDesc: String): Int {
        val localVariables = localVariables
            ?: throw composeClickTrackError(
                detail = "method <${this.name} $desc> 缺少 LocalVariableTable，无法按参数名定位 $paramName"
            )
        val matched = localVariables.filter { local ->
            local.name == paramName && local.desc == expectedDesc
        }
        if (matched.isEmpty()) {
            throw composeClickTrackError(
                detail = "method <${this.name} $desc> 未找到参数 $paramName:$expectedDesc"
            )
        }
        if (matched.size > 1) {
            throw composeClickTrackError(
                detail = "method <${this.name} $desc> 发现多个参数 $paramName:$expectedDesc，拒绝猜测"
            )
        }
        return matched[0].index
    }

    private fun composeClickTrackError(detail: String): IllegalStateException {
        return IllegalStateException(
            "composeClickTrack 插桩失败：$name 。$detail 。" +
                    "请确认 Compose Foundation 中 ClickableElement / CombinedClickableElement 构造参数仍包含 onClick / onClickLabel。"
        )
    }

    private fun insertInstructions(
        methodNode: MethodNode,
        onClickSlot: Int,
        onClickLabelSlot: Int
    ) {
        val input = InsnList()
        input.add(LdcInsnNode(trackConfig.skipOnClickLabel))
        input.add(VarInsnNode(Opcodes.ALOAD, onClickLabelSlot))
        input.add(
            MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/String",
                "equals",
                "(Ljava/lang/Object;)Z",
                false
            )
        )
        val clickWrapperClassFormat = replacePeriodWithSlash(className = trackConfig.clickWrapperClass)
        val label = LabelNode()
        input.add(JumpInsnNode(Opcodes.IFNE, label))
        input.add(TypeInsnNode(Opcodes.NEW, clickWrapperClassFormat))
        input.add(InsnNode(Opcodes.DUP))
        input.add(VarInsnNode(Opcodes.ALOAD, onClickSlot))
        input.add(
            MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                clickWrapperClassFormat,
                InitMethodName,
                "(Lkotlin/jvm/functions/Function0;)V",
                false
            )
        )
        input.add(VarInsnNode(Opcodes.ASTORE, onClickSlot))
        input.add(label)
        methodNode.instructions.insert(input)
    }

}