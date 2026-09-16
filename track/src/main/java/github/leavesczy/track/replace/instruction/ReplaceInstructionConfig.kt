package github.leavesczy.track.replace.instruction

import github.leavesczy.track.BaseTrackConfig
import java.io.Serializable

internal data class ReplaceInstructionConfig(
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
    var instructions: Set<ReplaceInstruction> = emptySet(),
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet()
)

open class ReplaceInstruction(
    var owner: String,
    var name: String,
    var descriptor: String,
    var proxyOwner: String
)

open class ToastPluginParameter(
    var proxyOwner: String = "",
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet()
)

open class OptimizedThreadPluginParameter(
    var proxyOwner: String = "",
    var methods: Set<String> = setOf(),
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet()
)