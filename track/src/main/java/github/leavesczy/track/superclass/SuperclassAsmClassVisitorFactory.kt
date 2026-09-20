package github.leavesczy.track.superclass

import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import github.leavesczy.track.BaseTrackAsmClassVisitorFactory
import github.leavesczy.track.utils.ASM_API
import github.leavesczy.track.utils.LogPrint
import github.leavesczy.track.utils.replacePeriodWithSlash
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 父类替换：把「直接继承 originClass」的类改为继承 targetClass，
 * 并改写对本父类的 INVOKESPECIAL（含 `<init>` 与 `super.xxx()`）。
 *
 * 不改间接继承链；也不改写 targetClass 自身（避免代理再被改）。
 */
internal abstract class SuperclassAsmClassVisitorFactory :
    BaseTrackAsmClassVisitorFactory<SuperclassConfigParameters, SuperclassConfig> {

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return SuperclassClassVisitor(
            nextClassVisitor = nextClassVisitor,
            trackConfig = trackConfig
        )
    }

    override fun isTrackEnabled(classData: ClassData): Boolean {
        val superClasses = classData.superClasses
        if (superClasses.isEmpty()) {
            return false
        }
        val isTargetClass = trackConfig.replacements.any { it.targetClass == classData.className }
        if (isTargetClass) {
            return false
        }
        // ClassData.superClasses 首项为直接父类。
        val directSuperClass = superClasses.first()
        return trackConfig.replacements.any { it.originClass == directSuperClass }
    }

}

private class SuperclassClassVisitor(
    nextClassVisitor: ClassVisitor,
    trackConfig: SuperclassConfig
) : ClassVisitor(ASM_API, nextClassVisitor) {

    private val replacementsByOrigin = trackConfig.replacements.associate { replacement ->
        replacePeriodWithSlash(className = replacement.originClass) to
                replacePeriodWithSlash(className = replacement.targetClass)
    }

    private var oldSuperName: String = ""

    private var newSuperName: String = ""

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        oldSuperName = superName.orEmpty()
        newSuperName = replacementsByOrigin[oldSuperName] ?: oldSuperName
        super.visit(
            version,
            access,
            name,
            replaceSuperTypeInSignature(signature = signature),
            newSuperName,
            interfaces
        )
        if (oldSuperName != newSuperName) {
            LogPrint.normal(tag = "superclassTrack") {
                "$name 的父类符合规则，完成处理..."
            }
        }
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (oldSuperName == newSuperName) {
            return methodVisitor
        }
        return object : MethodVisitor(ASM_API, methodVisitor) {
            override fun visitMethodInsn(
                opcode: Int,
                owner: String?,
                methodName: String?,
                methodDescriptor: String?,
                isInterface: Boolean
            ) {
                // 编译器生成的 super.xxx() / super(...) 都是 INVOKESPECIAL，且 owner 为直接父类。
                // 仅改写 <init> 会漏掉业务方法上的 super 调用，从而绕过新父类覆写。
                val currentOwner = if (opcode == Opcodes.INVOKESPECIAL && owner == oldSuperName) {
                    newSuperName
                } else {
                    owner
                }
                super.visitMethodInsn(
                    opcode,
                    currentOwner,
                    methodName,
                    methodDescriptor,
                    isInterface
                )
            }
        }
    }

    /** 同步泛型签名里的父类类型描述符，避免 signature 与 superName 不一致。 */
    private fun replaceSuperTypeInSignature(signature: String?): String? {
        if (signature.isNullOrEmpty() || oldSuperName == newSuperName) {
            return signature
        }
        val oldType = "L$oldSuperName;"
        val newType = "L$newSuperName;"
        if (!signature.contains(oldType)) {
            return signature
        }
        return signature.replace(oldType, newType)
    }

}
