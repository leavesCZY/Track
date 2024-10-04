package github.leavesczy.track.replace.instruction

import github.leavesczy.track.BaseTrackConfig
import java.io.Serializable

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal data class ReplaceInstructionConfig(
    override val isEnabled: Boolean,
    override val include: Set<String>,
    override val exclude: Set<String>,
    override val extensionName: String,
    val instructions: Set<ReplaceInstructionParameter>
) : BaseTrackConfig {

    data class ReplaceInstructionParameter(
        val owner: String,
        val name: String,
        val descriptor: String,
        val proxyOwner: String
    ) : Serializable

}

open class ReplaceInstructionPluginParameter(
    var isEnabled: Boolean = true,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet(),
    var instructions: Set<ReplaceInstruction> = emptySet()
)

open class ReplaceInstruction(
    var owner: String,
    var name: String,
    var descriptor: String,
    var proxyOwner: String
)

open class ToastPluginParameter(
    var isEnabled: Boolean = true,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet(),
    var proxyOwner: String = ""
)

open class OptimizedThreadPluginParameter(
    var isEnabled: Boolean = true,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet(),
    var proxyOwner: String = "",
    var methods: Set<String> = setOf(
        "newSingleThreadExecutor",
        "newCachedThreadPool",
        "newFixedThreadPool",
        "newScheduledThreadPool",
        "newSingleThreadScheduledExecutor"
    )
)