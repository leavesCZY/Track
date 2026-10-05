package github.leavesczy.track.viewclick

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.utils.filterLambda
import github.leavesczy.track.utils.hasAnnotation
import github.leavesczy.track.utils.isStatic
import github.leavesczy.track.utils.replacePeriodWithSlash
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode

/**
 * View 点击防抖：在 OnClickListener.onClick / 对应 lambda 方法入口插入闸门调用。
 *
 * 约定 clickHandler 签名为 `(Landroid/view/View;)Z`：返回 true 继续执行原逻辑，false 则直接 return。
 * Activity 上的 XML `android:onClick` 方法本身不会被改写；AppCompat 的 DeclaredOnClickListener
 * 若落在 include 范围内仍会被插桩。
 */
internal abstract class ViewClickAsmClassVisitorFactory :
    BaseTrackAsmClassVisitorFactory<ViewClickConfigParameters, ViewClickConfig> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return ViewClickClassVisitor(
            nextClassVisitor = nextClassVisitor,
            trackConfig = trackConfig
        )
    }

    override fun isTrackEnabled(classData: ClassData): Boolean {
        return true
    }

}

private class ViewClickClassVisitor(
    private val nextClassVisitor: ClassVisitor,
    override val trackConfig: ViewClickConfig
) : BaseTrackClassNode(trackConfig = trackConfig, logTag = "viewClickTrack") {

    private val viewObjectDesc = "Landroid/view/View;"

    private val onClickListenerInterfaceName = "android/view/View\$OnClickListener"

    private val onClickListenerInterfaceObjectDesc = "L${onClickListenerInterfaceName};"

    private val onClickMethodName = "onClick"

    private val onClickMethodDesc = "(Landroid/view/View;)V"

    private val clickHandlerMethodDesc = "(Landroid/view/View;)Z"

    override fun visitEnd() {
        super.visitEnd()
        handleViewClick()
        accept(nextClassVisitor)
    }

    private fun handleViewClick() {
        val shouldHookMethodList = mutableSetOf<MethodNode>()
        val methodsByNameDesc = HashMap<String, MethodNode>(methods.size)
        methods.forEach { methodNode ->
            methodsByNameDesc[methodNode.name + methodNode.desc] = methodNode
        }
        methods.forEach { methodNode ->
            when {
                methodNode.isSkipOnClick() -> {
                    // 带 skip 注解的 onClick 本身不插桩；方法体内的 lambda 仍会扫描。
                }
                methodNode.isViewOnClickMethod() -> {
                    shouldHookMethodList.add(element = methodNode)
                }
            }
            // lambda → OnClickListener 的 invokedynamic 中，bsmArgs[1] 指向实现方法。
            val dynamicNodes = methodNode.filterLambda {
                it.name == onClickMethodName && it.desc.endsWith(suffix = onClickListenerInterfaceObjectDesc)
            }
            dynamicNodes.forEach { node ->
                val handle = node.bsmArgs[1] as? Handle
                if (handle != null) {
                    val method = methodsByNameDesc[handle.name + handle.desc]
                    if (method != null && !method.isSkipOnClick()) {
                        shouldHookMethodList.add(element = method)
                    }
                }
            }
        }
        if (shouldHookMethodList.isNotEmpty()) {
            shouldHookMethodList.forEach {
                hookMethod(methodNode = it)
            }
            log {
                "$name 发现 ${shouldHookMethodList.size} 个 View.OnClickListener 指令，完成处理..."
            }
        }
    }

    private fun MethodNode.isSkipOnClick(): Boolean {
        val skipOnClickAnnotation = trackConfig.skipOnClickAnnotation
        return skipOnClickAnnotation.isNotBlank() &&
                hasAnnotation(annotationClassName = skipOnClickAnnotation)
    }

    private fun hookMethod(methodNode: MethodNode) {
        val argumentTypes = Type.getArgumentTypes(methodNode.desc)
        val viewArgumentIndex = argumentTypes.indexOfFirst {
            it.descriptor == viewObjectDesc
        }
        if (viewArgumentIndex >= 0) {
            val instructions = methodNode.instructions
            if (instructions != null && instructions.size() > 0) {
                val list = InsnList()
                list.add(
                    VarInsnNode(
                        Opcodes.ALOAD,
                        getVisitPosition(
                            argumentTypes,
                            viewArgumentIndex,
                            methodNode.isStatic
                        )
                    )
                )
                list.add(
                    MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        replacePeriodWithSlash(className = trackConfig.clickHandlerClass),
                        trackConfig.clickMethodName,
                        clickHandlerMethodDesc
                    )
                )
                val labelNode = LabelNode()
                list.add(JumpInsnNode(Opcodes.IFNE, labelNode))
                list.add(InsnNode(Opcodes.RETURN))
                list.add(labelNode)
                instructions.insert(list)
            }
        }
    }

    /** 形参下标 → 局部变量槽；实例方法 slot0 为 this，long/double 占两槽。 */
    private fun getVisitPosition(
        argumentTypes: Array<Type>,
        parameterIndex: Int,
        isStaticMethod: Boolean
    ): Int {
        if (parameterIndex < 0 || parameterIndex >= argumentTypes.size) {
            throw IllegalArgumentException("getVisitPosition error")
        }
        var slot = if (isStaticMethod) {
            0
        } else {
            1
        }
        for (index in 0 until parameterIndex) {
            slot += argumentTypes[index].size
        }
        return slot
    }

    private fun MethodNode.isViewOnClickMethod(): Boolean {
        val myInterfaces = interfaces
        if (myInterfaces.isNullOrEmpty()) {
            return false
        }
        return interfaces.contains(element = onClickListenerInterfaceName)
                && onClickMethodName == name && onClickMethodDesc == desc
    }

}
