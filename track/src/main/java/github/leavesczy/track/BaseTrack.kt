package github.leavesczy.track

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import github.leavesczy.track.utils.ApiOpcodes
import github.leavesczy.track.utils.LogPrint
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.tree.ClassNode
import java.io.Serializable

internal abstract class BaseTrackClassNode(protected open val trackConfig: BaseTrackConfig) :
    ClassNode(ApiOpcodes) {

    fun log(msg: () -> String) {
        LogPrint.normal(tag = trackConfig.extensionName, msg = msg)
    }

}

internal interface BaseTrackConfig : Serializable {

    val include: Set<String>

    val exclude: Set<String>

    val extensionName: String

}

internal interface BaseTrackConfigParameters<TrackConfig : BaseTrackConfig> :
    InstrumentationParameters {

    @get:Input
    val trackConfig: Property<TrackConfig>

}

internal interface BaseTrackAsmClassVisitorFactory<
        Parameters : BaseTrackConfigParameters<TrackConfig>,
        TrackConfig : BaseTrackConfig
        > : AsmClassVisitorFactory<Parameters> {

    @get:Input
    val trackConfig: TrackConfig
        get() = parameters.get().trackConfig.get()

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): BaseTrackClassNode

    override fun isInstrumentable(classData: ClassData): Boolean {
        val include = trackConfig.include.map {
            Regex(it)
        }
        val exclude = trackConfig.exclude.map {
            Regex(it)
        }
        if (include.isEmpty()) {
            if (classData.matches(rules = exclude)) {
                return false
            }
        } else if (exclude.isEmpty()) {
            if (!classData.matches(rules = include)) {
                return false
            }
        } else if (!classData.matches(rules = include) || classData.matches(rules = exclude)) {
            return false
        }
        return isTrackEnabled(classData = classData)
    }

    private fun ClassData.matches(rules: List<Regex>): Boolean {
        for (rule in rules) {
            if (className.matches(regex = rule)) {
                return true
            }
        }
        return false
    }

    fun isTrackEnabled(classData: ClassData): Boolean

}