package github.leavesczy.track.thread

import github.leavesczy.track.BaseTrackConfig
import github.leavesczy.track.utils.replaceDotBySlash

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal data class OptimizedThreadConfig(
    override val isEnabled: Boolean,
    override val include: Set<String>,
    override val exclude: Set<String>,
    val optimizedExecutorsClass: String,
    val executorsMethodNames: Set<String>
) : BaseTrackConfig {

    companion object {

        operator fun invoke(pluginParameter: OptimizedThreadPluginParameter): OptimizedThreadConfig? {
            val optimizedExecutorsClass = replaceDotBySlash(pluginParameter.optimizedExecutorsClass)
            val executorsMethodNames = pluginParameter.executorsMethodNames
            if (optimizedExecutorsClass.isBlank() || executorsMethodNames.isEmpty()) {
                return null
            }
            return OptimizedThreadConfig(
                isEnabled = pluginParameter.isEnabled,
                include = pluginParameter.include,
                exclude = pluginParameter.exclude,
                optimizedExecutorsClass = optimizedExecutorsClass,
                executorsMethodNames = executorsMethodNames
            )
        }

    }

}

open class OptimizedThreadPluginParameter(
    var isEnabled: Boolean = true,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet(),
    var optimizedExecutorsClass: String = "",
    var executorsMethodNames: Set<String> = setOf(
        "newSingleThreadExecutor",
        "newCachedThreadPool",
        "newFixedThreadPool",
        "newScheduledThreadPool",
        "newSingleThreadScheduledExecutor"
    )
)