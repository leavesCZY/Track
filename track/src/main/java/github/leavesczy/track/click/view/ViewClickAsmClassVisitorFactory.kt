package github.leavesczy.track.click.view

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.BaseTrackClassNode
import github.leavesczy.track.BaseTrackConfigParameters
import github.leavesczy.track.utils.LogPrint
import github.leavesczy.track.utils.filterLambda
import github.leavesczy.track.utils.getClassDesc
import github.leavesczy.track.utils.hasAnnotation
import github.leavesczy.track.utils.isStatic
import github.leavesczy.track.utils.nameWithDesc
import github.leavesczy.track.utils.replaceDotBySlash
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
            config = trackConfig
        )
    }

    override fun isTrackEnabled(classData: ClassData): Boolean {
        return true
    }

}

private class ViewClickClassVisitor(
    private val nextClassVisitor: ClassVisitor,
    private val config: ViewClickConfig,
) : BaseTrackClassNode() {

    private val viewClassDesc = getClassDesc(className = "android.view.View")

    private val onClickMethodDesc = "(Landroid/view/View;)Z"

    private val uncheckViewOnClickAnnotationDesc =
        getClassDesc(className = config.uncheckViewOnClickAnnotation)

    override fun visitEnd() {
        super.visitEnd()
        hook()
        accept(nextClassVisitor)
    }

    private fun hook() {
        val shouldHookMethodList = mutableSetOf<MethodNode>()
        methods.forEach { methodNode ->
            when {
                methodNode.hasUncheckViewOnClickAnnotation() -> {

                }

                methodNode.isHookPoint() -> {
                    shouldHookMethodList.add(element = methodNode)
                }
            }
            val dynamicNodes = methodNode.filterLambda {
                val nodeName = it.name
                val nodeDesc = it.desc
                config.hookPointList.any { point ->
                    nodeName == point.methodName && nodeDesc.endsWith(suffix = "L${point.interfaceName};")
                }
            }
            dynamicNodes.forEach { node ->
                val handle = node.bsmArgs[1] as? Handle
                if (handle != null) {
                    val nameWithDesc = handle.name + handle.desc
                    val method = methods.find { method ->
                        method.nameWithDesc == nameWithDesc
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
            LogPrint.normal(tag = "ViewClickTrack") {
                "$name 发现 ${shouldHookMethodList.size} 个 View.OnClickListener 指令，完成处理..."
            }
        }
    }

    private fun hookMethod(modeNode: MethodNode) {
        val argumentTypes = Type.getArgumentTypes(modeNode.desc)
        val viewArgumentIndex = argumentTypes?.indexOfFirst {
            it.descriptor == viewClassDesc
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
                        replaceDotBySlash(className = config.onClickClass),
                        config.onClickMethodName,
                        onClickMethodDesc
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
        val methodNameWithDesc = nameWithDesc
        return config.hookPointList.any {
            myInterfaces.contains(element = it.interfaceName) &&
                    methodNameWithDesc == it.methodNameWithDesc
        }
    }

    private fun MethodNode.hasUncheckViewOnClickAnnotation(): Boolean {
        return hasAnnotation(annotationDesc = uncheckViewOnClickAnnotationDesc)
    }

}