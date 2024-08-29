package github.leavesczy.track

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import github.leavesczy.track.utils.LogPrint
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.io.Serializable

/**
 * @Author: leavesCZY
 * @Date: 2024/8/25 1:56
 * @Desc:
 */
internal abstract class BaseTrackClassNode(protected open val trackConfig: BaseTrackConfig) :
    ClassNode(Opcodes.ASM7) {

    fun nLog(msg: () -> Any) {
        LogPrint.normal(tag = trackConfig.extensionName, msg = msg)
    }

}

internal interface BaseTrackConfig : Serializable {

    val isEnabled: Boolean

    val include: Set<String>

    val exclude: Set<String>

    val extensionName: String

}

internal interface BaseTrackConfigParameters : InstrumentationParameters {
    @get:Input
    val trackConfig: Property<BaseTrackConfig>
}

internal interface BaseTrackAsmClassVisitorFactory<Parameters : BaseTrackConfigParameters, TrackConfig : BaseTrackConfig> :
    AsmClassVisitorFactory<Parameters> {

    @get:Input
    val trackConfig: TrackConfig
        get() = parameters.get().trackConfig.get() as TrackConfig

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): BaseTrackClassNode

    override fun isInstrumentable(classData: ClassData): Boolean {
        if (!trackConfig.isEnabled) {
            return false
        }
        val include = trackConfig.include
        val exclude = trackConfig.exclude
        if (include.isEmpty()) {
            if (classData.matches(rules = exclude)) {
                return false
            }
        } else {
            if (exclude.isEmpty()) {
                if (!classData.matches(rules = include)) {
                    return false
                }
            } else {
                if (!classData.matches(rules = include) || classData.matches(rules = exclude)) {
                    return false
                }
            }
        }
        return isTrackEnabled(classData = classData)
    }

    private fun ClassData.matches(rules: Collection<String>): Boolean {
        for (item in rules) {
            val regex = Regex(item)
            if (className.matches(regex = regex)) {
                return true
            }
        }
        return false
    }

    fun isTrackEnabled(classData: ClassData): Boolean

}