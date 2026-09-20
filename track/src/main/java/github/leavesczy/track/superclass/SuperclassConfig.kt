package github.leavesczy.track.superclass

import github.leavesczy.track.BaseTrackConfig
import github.leavesczy.track.BaseTrackConfigParameters
import java.io.Serializable

internal data class SuperclassConfig(
    override val include: Set<String>,
    override val exclude: Set<String>,
    val replacements: Set<SuperclassReplacement>
) : BaseTrackConfig {

    data class SuperclassReplacement(
        val originClass: String,
        val targetClass: String
    ) : Serializable

}

/** 将直接继承 [originClass] 的类改为继承 [targetClass]。 */
data class SuperclassRule(
    var originClass: String,
    var targetClass: String,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet()
)

open class SuperclassTrackPluginParameter(
    var rules: Set<SuperclassRule> = emptySet()
)

internal interface SuperclassConfigParameters : BaseTrackConfigParameters<SuperclassConfig>
