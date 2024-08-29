package github.leavesczy.track.replace.instruction

import github.leavesczy.track.BaseTrackConfig
import github.leavesczy.track.utils.replaceDotBySlash
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

    companion object {

        private fun ReplaceInstruction.mapInstruction(): ReplaceInstructionParameter? {
            return if (owner.isBlank() || name.isBlank() || proxyOwner.isBlank()) {
                null
            } else {
                ReplaceInstructionParameter(
                    owner = replaceDotBySlash(className = owner),
                    name = name,
                    descriptor = descriptor,
                    proxyOwner = proxyOwner
                )
            }
        }

        operator fun invoke(
            pluginParameter: ReplaceInstructionPluginParameter,
            extensionName: String
        ): ReplaceInstructionConfig? {
            val instructions = pluginParameter.instructions.mapNotNull {
                it.mapInstruction()
            }.toSet()
            if (instructions.isEmpty()) {
                return null
            }
            return ReplaceInstructionConfig(
                isEnabled = pluginParameter.isEnabled,
                include = pluginParameter.include,
                exclude = pluginParameter.exclude,
                extensionName = extensionName,
                instructions = instructions
            )
        }

        operator fun invoke(
            pluginParameter: OptimizedThreadPluginParameter,
            extensionName: String
        ): ReplaceInstructionConfig? {
            val optimizedExecutorsClass = pluginParameter.optimizedExecutorsClass
            val executorsMethods = pluginParameter.executorsMethods
            if (optimizedExecutorsClass.isBlank() || executorsMethods.isEmpty()) {
                return null
            }
            val instructions = executorsMethods.map {
                ReplaceInstructionParameter(
                    owner = "java/util/concurrent/Executors",
                    name = it,
                    descriptor = "",
                    proxyOwner = replaceDotBySlash(className = optimizedExecutorsClass)
                )
            }.toSet()
            return ReplaceInstructionConfig(
                isEnabled = pluginParameter.isEnabled,
                include = pluginParameter.include,
                exclude = pluginParameter.exclude,
                extensionName = extensionName,
                instructions = instructions
            )
        }

    }

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

open class OptimizedThreadPluginParameter(
    var isEnabled: Boolean = true,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet(),
    var optimizedExecutorsClass: String = "",
    var executorsMethods: Set<String> = setOf(
        "newSingleThreadExecutor",
        "newCachedThreadPool",
        "newFixedThreadPool",
        "newScheduledThreadPool",
        "newSingleThreadScheduledExecutor"
    )
)