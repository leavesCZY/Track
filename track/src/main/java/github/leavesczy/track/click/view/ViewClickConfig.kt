package github.leavesczy.track.click.view

import github.leavesczy.track.BaseTrackConfig

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal data class ViewClickConfig(
    override val isEnabled: Boolean,
    override val include: Set<String>,
    override val exclude: Set<String>,
    override val extensionName: String,
    val onClickClass: String,
    val onClickMethodName: String,
    val uncheckViewOnClickAnnotation: String
) : BaseTrackConfig {

    companion object {

        operator fun invoke(
            pluginParameter: ViewClickPluginParameter,
            extensionName: String
        ): ViewClickConfig? {
            val onClickClass = pluginParameter.onClickClass
            val onClickMethodName = pluginParameter.onClickMethodName
            return if (onClickClass.isBlank() || onClickMethodName.isBlank()) {
                null
            } else {
                ViewClickConfig(
                    isEnabled = pluginParameter.isEnabled,
                    include = pluginParameter.include,
                    exclude = pluginParameter.exclude,
                    extensionName = extensionName,
                    onClickClass = onClickClass,
                    onClickMethodName = onClickMethodName,
                    uncheckViewOnClickAnnotation = pluginParameter.uncheckViewOnClickAnnotation
                )
            }
        }

    }

}

open class ViewClickPluginParameter(
    var isEnabled: Boolean = true,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet(),
    var onClickClass: String = "",
    var onClickMethodName: String = "",
    var uncheckViewOnClickAnnotation: String = ""
)