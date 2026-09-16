package github.leavesczy.track.click.compose

import github.leavesczy.track.BaseTrackConfig
import github.leavesczy.track.BaseTrackConfigParameters

internal data class ComposeClickConfig(
    override val include: Set<String>,
    override val exclude: Set<String>,
    override val extensionName: String,
    val clickWrapperClass: String,
    val skipOnClickLabel: String
) : BaseTrackConfig

open class ComposeClickTrackPluginParameter(
    var clickWrapperClass: String = "",
    var skipOnClickLabel: String = ""
)

internal interface ComposeClickConfigParameters : BaseTrackConfigParameters<ComposeClickConfig>
