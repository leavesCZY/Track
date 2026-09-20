package github.leavesczy.track.utils

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.MethodNode

internal const val INIT_METHOD_NAME = "<init>"

internal const val ASM_API = Opcodes.ASM9

internal val MethodNode.isStatic: Boolean
    get() = access and Opcodes.ACC_STATIC == Opcodes.ACC_STATIC

/** 点分全限定名 → ASM 内部名。 */
internal fun replacePeriodWithSlash(className: String): String {
    return className.replace(".", "/")
}

/** 同时检查 RuntimeVisible / Invisible 注解（Kotlin BINARY 保留常见于后者）。 */
internal fun MethodNode.hasAnnotation(annotationClassName: String): Boolean {
    val annotationDesc =
        Type.getObjectType(replacePeriodWithSlash(className = annotationClassName)).descriptor
    val visible = visibleAnnotations?.any { it.desc == annotationDesc } == true
    if (visible) {
        return true
    }
    return invisibleAnnotations?.any { it.desc == annotationDesc } == true
}

/** 收集方法体中的 invokedynamic（用于定位 SAM / lambda 实现方法）。 */
internal fun MethodNode.filterLambda(filter: (InvokeDynamicInsnNode) -> Boolean): List<InvokeDynamicInsnNode> {
    val methodInstructions = instructions
    if (methodInstructions == null || methodInstructions.size() == 0) {
        return emptyList()
    }
    val dynamicList = mutableListOf<InvokeDynamicInsnNode>()
    methodInstructions.forEach { instruction ->
        if (instruction is InvokeDynamicInsnNode) {
            if (filter(instruction)) {
                dynamicList.add(element = instruction)
            }
        }
    }
    return dynamicList
}
