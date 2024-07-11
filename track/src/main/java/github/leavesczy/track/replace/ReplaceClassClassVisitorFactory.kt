package github.leavesczy.track.replace

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import github.leavesczy.track.utils.LogPrint
import github.leavesczy.track.utils.matches
import github.leavesczy.track.utils.replaceDotBySlash
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal abstract class ReplaceClassClassVisitorFactory :
    AsmClassVisitorFactory<ReplaceClassClassVisitorFactory.ReplaceClassConfigParameters> {

    interface ReplaceClassConfigParameters : InstrumentationParameters {
        @get:Input
        val config: Property<ReplaceClassConfig>
    }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return ReplaceClassClassVisitor(
            config = parameters.get().config.get(),
            nextClassVisitor = nextClassVisitor
        )
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val config = parameters.get().config.get()
        val className = classData.className
        val superClasses = classData.superClasses
        if (className == config.targetClass || superClasses.first() != config.originClass) {
            return false
        }
        val include = config.include
        val exclude = config.exclude
        if (include.isEmpty()) {
            return !classData.matches(rules = exclude)
        }
        return classData.matches(rules = include) && !classData.matches(rules = exclude)
    }

}

private class ReplaceClassClassVisitor(
    private val config: ReplaceClassConfig,
    private val nextClassVisitor: ClassVisitor
) : ClassNode(Opcodes.ASM5) {

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(
            version,
            access,
            name,
            signature,
            replaceDotBySlash(className = config.targetClass),
            interfaces
        )
        LogPrint.normal(tag = "ReplaceClassTrack") {
            "$name 的父类符合 ReplaceClass 规则，完成处理..."
        }
    }

    override fun visitEnd() {
        super.visitEnd()
        accept(nextClassVisitor)
    }

}