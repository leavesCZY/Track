package github.leavesczy.track.click.view

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.BaseTrackConfigParameters
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
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal abstract class ViewClickAsmClassVisitorFactory :
    BaseTrackAsmClassVisitorFactory<BaseTrackConfigParameters, ViewClickConfig> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): BaseTrackClassNode {
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
) : BaseTrackClassNode(trackConfig = trackConfig) {

    private val viewObjectDesc = "Landroid/view/View;"

    private val onClickListenerInterfaceName = "android/view/View\$OnClickListener"

    private val onClickListenerInterfaceObjectDes = "L${onClickListenerInterfaceName};"

    private val onClickMethodName = "onClick"

    private val onClickMethodDesc = "(Landroid/view/View;)V"

    private val proxyOnClickMethodDesc = "(Landroid/view/View;)Z"

    override fun visitEnd() {
        super.visitEnd()
        handleViewClick()
        accept(nextClassVisitor)
    }

    private fun handleViewClick() {
        val shouldHookMethodList = mutableSetOf<MethodNode>()
        methods.forEach { methodNode ->
            when {
                methodNode.hasAnnotation(annotationClassName = trackConfig.uncheckViewOnClickAnnotation) -> {

                }

                methodNode.isHookPoint() -> {
                    shouldHookMethodList.add(element = methodNode)
                }
            }
            val dynamicNodes = methodNode.filterLambda {
                it.name == onClickMethodName && it.desc.endsWith(suffix = onClickListenerInterfaceObjectDes)
            }
            dynamicNodes.forEach { node ->
                val handle = node.bsmArgs[1] as? Handle
                if (handle != null) {
                    val nameWithDesc = handle.name + handle.desc
                    val method = methods.find { method ->
                        method.name + method.desc == nameWithDesc
                    }
                    if (method != null) {
                        shouldHookMethodList.add(element = method)
                    }
                }
            }
        }
        if (shouldHookMethodList.isNotEmpty()) {
            shouldHookMethodList.forEach {
                hookMethod(modeNode = it)
            }
            log {
                "$name 发现 ${shouldHookMethodList.size} 个 View.OnClickListener 指令，完成处理..."
            }
        }
    }

    private fun hookMethod(modeNode: MethodNode) {
        val argumentTypes = Type.getArgumentTypes(modeNode.desc)
        val viewArgumentIndex = argumentTypes?.indexOfFirst {
            it.descriptor == viewObjectDesc
        } ?: -1
        if (viewArgumentIndex >= 0) {
            val instructions = modeNode.instructions
            if (instructions != null && instructions.size() > 0) {
                val list = InsnList()
                list.add(
                    VarInsnNode(
                        Opcodes.ALOAD,
                        getVisitPosition(
                            argumentTypes,
                            viewArgumentIndex,
                            modeNode.isStatic
                        )
                    )
                )
                list.add(
                    MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        replacePeriodWithSlash(className = trackConfig.onClickClass),
                        trackConfig.onClickMethodName,
                        proxyOnClickMethodDesc
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

    private fun getVisitPosition(
        argumentTypes: Array<Type>,
        parameterIndex: Int,
        isStaticMethod: Boolean
    ): Int {
        if (parameterIndex < 0 || parameterIndex >= argumentTypes.size) {
            throw Error("getVisitPosition error")
        }
        return if (parameterIndex == 0) {
            if (isStaticMethod) {
                0
            } else {
                1
            }
        } else {
            getVisitPosition(
                argumentTypes,
                parameterIndex - 1,
                isStaticMethod
            ) + argumentTypes[parameterIndex - 1].size
        }
    }

    private fun MethodNode.isHookPoint(): Boolean {
        val myInterfaces = interfaces
        if (myInterfaces.isNullOrEmpty()) {
            return false
        }
        return interfaces.contains(element = onClickListenerInterfaceName)
                && onClickMethodName == name && onClickMethodDesc == desc
    }

}