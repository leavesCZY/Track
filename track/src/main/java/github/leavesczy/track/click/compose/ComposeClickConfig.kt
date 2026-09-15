package github.leavesczy.track.click.compose

import github.leavesczy.track.BaseTrackConfig

internal data class ComposeClickConfig(
    override val include: Set<String>,
    override val exclude: Set<String>,
    override val extensionName: String,
    val onClickClass: String,
    val uncheckOnClickLabel: String
) : BaseTrackConfig

open class ComposeClickPluginParameter(
    var onClickClass: String = "",
    var uncheckOnClickLabel: String = ""
)