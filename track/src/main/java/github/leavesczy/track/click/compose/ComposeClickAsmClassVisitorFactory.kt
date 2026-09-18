package github.leavesczy.track.click.compose

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.utils.INIT_METHOD_NAME
import github.leavesczy.track.utils.replacePeriodWithSlash
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FrameNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

private const val CLICKABLE_ELEMENT_CLASS_NAME = "androidx.compose.foundation.ClickableElement"

private const val COMBINED_CLICKABLE_ELEMENT_CLASS_NAME =
    "androidx.compose.foundation.CombinedClickableElement"

private const val ON_CLICK_LABEL_FIELD_NAME = "onClickLabel"

private const val ON_CLICK_FIELD_NAME = "onClick"

private const val STRING_DESC = "Ljava/lang/String;"

private const val FUNCTION0_DESC = "Lkotlin/jvm/functions/Function0;"

private const val DEFAULT_CONSTRUCTOR_MARKER_CLASS_NAME =
    "kotlin.jvm.internal.DefaultConstructorMarker"

internal abstract class ComposeClickAsmClassVisitorFactory :
    BaseTrackAsmClassVisitorFactory<ComposeClickConfigParameters, ComposeClickConfig> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
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
) : BaseTrackClassNode(trackConfig = trackConfig, logTag = "composeClickTrack") {

    override fun visitEnd() {
        super.visitEnd()
        val primaryConstructors = methods.filter { methodNode ->
            methodNode.name == INIT_METHOD_NAME && methodNode.isPrimaryClickableConstructor()
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
        return findPutFieldInsns(
            fieldName = ON_CLICK_FIELD_NAME,
            fieldDesc = FUNCTION0_DESC
        ).isNotEmpty()
    }

    private fun handleComposeClick(methodNode: MethodNode) {
        val onClickSlot = methodNode.findSlotByPutField(
            fieldName = ON_CLICK_FIELD_NAME,
            fieldDesc = FUNCTION0_DESC
        )
        val onClickLabelSlot = if (trackConfig.skipOnClickLabel.isNotEmpty()) {
            methodNode.findSlotByPutField(
                fieldName = ON_CLICK_LABEL_FIELD_NAME,
                fieldDesc = STRING_DESC
            )
        } else {
            -1
        }
        insertInstructions(
            methodNode = methodNode,
            onClickLabelSlot = onClickLabelSlot,
            onClickSlot = onClickSlot
        )
    }

    /**
     * 从 `PUTFIELD fieldName` 向前反推为其供值的 `ALOAD` 槽位。
     * 按字段名区分 CombinedClickableElement 中多个 Function0，不依赖 LVT 参数名。
     */
    private fun MethodNode.findSlotByPutField(fieldName: String, fieldDesc: String): Int {
        val putFields = findPutFieldInsns(fieldName = fieldName, fieldDesc = fieldDesc)
        if (putFields.isEmpty()) {
            throw composeClickTrackError(
                detail = "method <${this.name} $desc> 未找到 putfield $fieldName:$fieldDesc"
            )
        }
        if (putFields.size > 1) {
            throw composeClickTrackError(
                detail = "method <${this.name} $desc> 发现多处 putfield $fieldName:$fieldDesc，拒绝猜测"
            )
        }
        val valueLoader = putFields[0].findPrecedingValueLoader()
            ?: throw composeClickTrackError(
                detail = "method <${this.name} $desc> 无法从 putfield $fieldName:$fieldDesc 反推供值指令"
            )
        if (valueLoader.opcode != Opcodes.ALOAD) {
            throw composeClickTrackError(
                detail = "method <${this.name} $desc> putfield $fieldName:$fieldDesc 的供值不是 ALOAD（opcode=${valueLoader.opcode}），拒绝继续编译"
            )
        }
        val slot = valueLoader.`var`
        if (!isConstructorParameterSlot(slot = slot)) {
            throw composeClickTrackError(
                detail = "method <${this.name} $desc> putfield $fieldName:$fieldDesc 反推到的槽位 $slot 不是构造参数槽，拒绝继续编译"
            )
        }
        return slot
    }

    private fun MethodNode.findPutFieldInsns(
        fieldName: String,
        fieldDesc: String
    ): List<FieldInsnNode> {
        val classInternalName = this@ComposeClickClassVisitor.name
        return instructions.mapNotNull { insn ->
            if (insn is FieldInsnNode &&
                insn.opcode == Opcodes.PUTFIELD &&
                insn.owner == classInternalName &&
                insn.name == fieldName &&
                insn.desc == fieldDesc
            ) {
                insn
            } else {
                null
            }
        }
    }

    private fun FieldInsnNode.findPrecedingValueLoader(): VarInsnNode? {
        var insn: AbstractInsnNode? = previous
        while (insn != null && insn.isIgnorable()) {
            insn = insn.previous
        }
        if (insn is TypeInsnNode && insn.opcode == Opcodes.CHECKCAST) {
            insn = insn.previous
            while (insn != null && insn.isIgnorable()) {
                insn = insn.previous
            }
        }
        return insn as? VarInsnNode
    }

    private fun AbstractInsnNode.isIgnorable(): Boolean {
        return this is LabelNode || this is LineNumberNode || this is FrameNode
    }

    private fun MethodNode.isConstructorParameterSlot(slot: Int): Boolean {
        if (slot < 1) {
            return false
        }
        var nextSlot = 1
        Type.getArgumentTypes(desc).forEach { argumentType ->
            if (slot == nextSlot) {
                return true
            }
            nextSlot += argumentType.size
        }
        return false
    }

    private fun composeClickTrackError(detail: String): IllegalStateException {
        return IllegalStateException(
            "composeClickTrack 插桩失败：$name 。$detail 。" +
                    "请确认 Compose Foundation 中 ClickableElement / CombinedClickableElement 仍会 putfield onClick / onClickLabel。"
        )
    }

    private fun insertInstructions(
        methodNode: MethodNode,
        onClickSlot: Int,
        onClickLabelSlot: Int
    ) {
        val input = InsnList()
        val clickWrapperClassFormat =
            replacePeriodWithSlash(className = trackConfig.clickWrapperClass)
        val afterWrap = LabelNode()
        val skipOnClickLabel = trackConfig.skipOnClickLabel
        if (skipOnClickLabel.isNotEmpty()) {
            input.add(LdcInsnNode(skipOnClickLabel))
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
            input.add(JumpInsnNode(Opcodes.IFNE, afterWrap))
        }
        input.add(TypeInsnNode(Opcodes.NEW, clickWrapperClassFormat))
        input.add(InsnNode(Opcodes.DUP))
        input.add(VarInsnNode(Opcodes.ALOAD, onClickSlot))
        input.add(
            MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                clickWrapperClassFormat,
                INIT_METHOD_NAME,
                "(Lkotlin/jvm/functions/Function0;)V",
                false
            )
        )
        input.add(VarInsnNode(Opcodes.ASTORE, onClickSlot))
        input.add(afterWrap)
        methodNode.instructions.insert(input)
    }

}
