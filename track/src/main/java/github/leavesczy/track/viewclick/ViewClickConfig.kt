package github.leavesczy.track.viewclick

import github.leavesczy.track.BaseTrackConfig
import github.leavesczy.track.BaseTrackConfigParameters

internal data class ViewClickConfig(
    override val include: Set<String>,
    override val exclude: Set<String>,
    val clickHandlerClass: String,
    val clickMethodName: String,
    val skipOnClickAnnotation: String
) : BaseTrackConfig

/**
 * viewClickTrack DSL。
 *
 * [clickHandlerClass] / [clickMethodName] 须提供静态方法 `(View)Z`。
 * [skipOnClickAnnotation] 可选，标在 OnClickListener.onClick 实现上可跳过防抖。
 */
open class ViewClickTrackPluginParameter(
    var clickHandlerClass: String = "",
    var clickMethodName: String = "",
    var skipOnClickAnnotation: String = "",
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet()
)

internal interface ViewClickConfigParameters : BaseTrackConfigParameters<ViewClickConfig>
