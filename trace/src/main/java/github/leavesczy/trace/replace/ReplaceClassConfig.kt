package github.leavesczy.trace.replace

import java.io.Serializable

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal data class ReplaceClassConfig(
    val originClass: String,
    val targetClass: String,
    val include: List<String>,
    val exclude: List<String>
) : Serializable {

    companion object {

        operator fun invoke(pluginParameter: ReplaceClassPluginParameter): ReplaceClassConfig? {
            val originClass = pluginParameter.originClass
            val targetClass = pluginParameter.targetClass
            if (originClass.isBlank() || targetClass.isBlank()) {
                return null
            }
            return ReplaceClassConfig(
                originClass = originClass,
                targetClass = targetClass,
                include = pluginParameter.include,
                exclude = pluginParameter.exclude
            )
        }

    }

}

open class ReplaceClassPluginParameter(
    var originClass: String = "",
    var targetClass: String = "",
    var include: List<String> = listOf(),
    var exclude: List<String> = listOf()
)