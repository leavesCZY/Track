package github.leavesczy.track.replace.clazz

import github.leavesczy.track.BaseTrackConfig

/**
 * @Author: leavesCZY
 * @Date: 2025/5/16 11:42
 * @Desc:
 */
internal data class ReplaceClassConfig(
    override val isEnabled: Boolean,
    override val include: Set<String>,
    override val exclude: Set<String>,
    override val extensionName: String,
    val originClass: String,
    val targetClass: String
) : BaseTrackConfig

open class ReplaceClassPluginParameter(
    var isEnabled: Boolean = true,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet(),
    var originClass: String = "",
    var targetClass: String = ""
)