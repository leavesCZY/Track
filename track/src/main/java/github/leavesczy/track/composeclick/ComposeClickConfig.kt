package github.leavesczy.track.composeclick

import github.leavesczy.track.BaseTrackConfig
import github.leavesczy.track.BaseTrackConfigParameters

internal data class ComposeClickConfig(
    override val include: Set<String>,
    override val exclude: Set<String>,
    val clickWrapperClass: String,
    val skipOnClickLabel: String
) : BaseTrackConfig

/**
 * composeClickTrack DSL。
 *
 * [clickWrapperClass] 须提供接收 `Function0` 的构造。
 * [skipOnClickLabel] 非空时，`Modifier.clickable(onClickLabel = …)` 等于该值则不包装。
 */
open class ComposeClickTrackPluginParameter(
    var clickWrapperClass: String = "",
    var skipOnClickLabel: String = ""
)

internal interface ComposeClickConfigParameters : BaseTrackConfigParameters<ComposeClickConfig>
