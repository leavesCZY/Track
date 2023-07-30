package github.leavesczy.trace.click

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import github.leavesczy.trace.utils.filterLambda
import github.leavesczy.trace.utils.getClassDesc
import github.leavesczy.trace.utils.hasAnnotation
import github.leavesczy.trace.utils.isStatic
import github.leavesczy.trace.utils.matches
import github.leavesczy.trace.utils.nameWithDesc
import github.leavesczy.trace.utils.replaceDotBySlash
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
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
internal interface ViewClickConfigParameters : InstrumentationParameters {
    @get:Input
    val config: Property<ViewClickConfig>
}

internal abstract class ViewClickClassVisitorFactory :
    AsmClassVisitorFactory<ViewClickConfigParameters> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val config = parameters.get().config.get()
        return ViewClickClassVisitor(
            nextClassVisitor = nextClassVisitor,
            config = config
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val config = parameters.get().config.get()
        val include = config.include
        val exclude = config.exclude
        if (include.isEmpty()) {
            return !classData.matches(rules = exclude)
        }
        return classData.matches(rules = include) &&
                !classData.matches(rules = exclude)
    }

}

private class ViewClickClassVisitor(
    private val nextClassVisitor: ClassVisitor,
    private val config: ViewClickConfig,
) : ClassNode(Opcodes.ASM5) {

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
        shouldHookMethodList.forEach {
            hookMethod(modeNode = it)
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