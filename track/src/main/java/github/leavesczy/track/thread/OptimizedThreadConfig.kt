package github.leavesczy.track.thread

import github.leavesczy.track.BaseTrackConfig

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal data class OptimizedThreadConfig(
    override val isEnabled: Boolean,
    override val include: Set<String>,
    override val exclude: Set<String>,
    val optimizedThreadClass: String,
    val optimizedExecutorsClass: String,
    val executorsMethodNames: Set<String>
) : BaseTrackConfig {

    companion object {

        operator fun invoke(pluginParameter: OptimizedThreadPluginParameter): OptimizedThreadConfig? {
            val optimizedThreadClass = pluginParameter.optimizedThreadClass.replace(".", "/")
            val optimizedExecutorsClass = pluginParameter.optimizedExecutorsClass.replace(
                ".",
                "/"
            )
            val executorsMethodNames = pluginParameter.executorsMethodNames
            if (optimizedThreadClass.isBlank() || optimizedExecutorsClass.isBlank() || executorsMethodNames.isEmpty()) {
                return null
            }
            return OptimizedThreadConfig(
                isEnabled = pluginParameter.isEnabled,
                include = pluginParameter.include,
                exclude = pluginParameter.exclude,
                optimizedThreadClass = optimizedThreadClass,
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
    var optimizedThreadClass: String = "",
    var optimizedExecutorsClass: String = "",
    var executorsMethodNames: Set<String> = setOf(
        "newFixedThreadPool",
        "newSingleThreadExecutor",
        "newCachedThreadPool",
        "newSingleThreadScheduledExecutor",
        "newScheduledThreadPool"
    )
)