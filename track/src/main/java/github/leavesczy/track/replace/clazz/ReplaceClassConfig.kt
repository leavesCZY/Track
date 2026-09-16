package github.leavesczy.track.replace.clazz

import github.leavesczy.track.BaseTrackConfig

internal data class ReplaceClassConfig(
    override val include: Set<String>,
    override val exclude: Set<String>,
    override val extensionName: String,
    val originClass: String,
    val targetClass: String
) : BaseTrackConfig

open class ReplaceClassPluginParameter(
    var originClass: String = "",
    var targetClass: String = "",
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet()
)