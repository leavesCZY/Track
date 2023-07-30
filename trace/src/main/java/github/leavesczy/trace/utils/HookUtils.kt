package github.leavesczy.trace.utils

import com.android.build.api.instrumentation.ClassData
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.MethodNode

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal const val InitMethodName = "<init>"

internal val MethodNode.nameWithDesc: String
    get() = name + desc

internal val MethodNode.isStatic: Boolean
    get() = access and Opcodes.ACC_STATIC == Opcodes.ACC_STATIC

internal fun replaceDotBySlash(className: String): String {
    return className.replace(".", "/")
}

internal fun getClassDesc(className: String): String {
    return "L" + className.replace(".", "/") + ";"
}

internal fun MethodNode.hasAnnotation(annotationDesc: String): Boolean {
    return visibleAnnotations?.find { it.desc == annotationDesc } != null
}

internal fun MethodNode.filterLambda(filter: (InvokeDynamicInsnNode) -> Boolean): List<InvokeDynamicInsnNode> {
    val mInstructions = instructions
    if (mInstructions == null || mInstructions.size() == 0) {
        return emptyList()
    }
    val dynamicList = mutableListOf<InvokeDynamicInsnNode>()
    mInstructions.forEach { instruction ->
        if (instruction is InvokeDynamicInsnNode) {
            if (filter(instruction)) {
                dynamicList.add(element = instruction)
            }
        }
    }
    return dynamicList
}

internal fun ClassData.matches(rules: List<String>): Boolean {
    if (rules.isEmpty()) {
        return false
    }
    for (item in rules) {
        val regex = Regex(item)
        if (className.matches(regex = regex)) {
            return true
        }
    }
    return false
}