package github.leavesczy.track.click.view

import github.leavesczy.track.BaseTrackConfig
import github.leavesczy.track.BaseTrackConfigParameters

internal data class ViewClickConfig(
    override val include: Set<String>,
    override val exclude: Set<String>,
    override val extensionName: String,
    val clickHandlerClass: String,
    val clickMethodName: String,
    val skipOnClickAnnotation: String
) : BaseTrackConfig

open class ViewClickTrackPluginParameter(
    var clickHandlerClass: String = "",
    var clickMethodName: String = "",
    var skipOnClickAnnotation: String = "",
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet()
)

internal interface ViewClickConfigParameters : BaseTrackConfigParameters<ViewClickConfig>
