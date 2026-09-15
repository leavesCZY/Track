package github.leavesczy.track.click.compose

import github.leavesczy.track.BaseTrackConfig

internal data class ComposeClickConfig(
    override val isEnabled: Boolean,
    override val include: Set<String>,
    override val exclude: Set<String>,
    override val extensionName: String,
    val onClickClass: String,
    val onClickWhiteList: String
) : BaseTrackConfig

open class ComposeClickPluginParameter(
    var isEnabled: Boolean = true,
    var onClickClass: String = "",
    var onClickWhiteList: String = ""
)