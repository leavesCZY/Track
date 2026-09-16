package github.leavesczy.track.click.view

import github.leavesczy.track.BaseTrackConfig
import github.leavesczy.track.BaseTrackConfigParameters

internal data class ViewClickConfig(
    override val include: Set<String>,
    override val exclude: Set<String>,
    override val extensionName: String,
    val onClickClass: String,
    val onClickMethodName: String,
    val uncheckViewOnClickAnnotation: String
) : BaseTrackConfig

open class ViewClickPluginParameter(
    var onClickClass: String = "",
    var onClickMethodName: String = "",
    var uncheckViewOnClickAnnotation: String = "",
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet()
)

internal interface ViewClickConfigParameters : BaseTrackConfigParameters<ViewClickConfig>
