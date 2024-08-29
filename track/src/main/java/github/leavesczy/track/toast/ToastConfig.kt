package github.leavesczy.track.toast

import github.leavesczy.track.BaseTrackConfig

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal data class ToastConfig(
    override val isEnabled: Boolean,
    override val include: Set<String>,
    override val exclude: Set<String>,
    override val extensionName: String,
    val toasterClass: String,
    val showToastMethodName: String
) : BaseTrackConfig {

    companion object {

        operator fun invoke(
            pluginParameter: ToastPluginParameter,
            extensionName: String
        ): ToastConfig? {
            val toasterClass = pluginParameter.toasterClass
            val showToastMethodName = pluginParameter.showToastMethodName
            if (toasterClass.isBlank() || showToastMethodName.isBlank()) {
                return null
            }
            return ToastConfig(
                isEnabled = pluginParameter.isEnabled,
                include = pluginParameter.include,
                exclude = pluginParameter.exclude,
                extensionName = extensionName,
                toasterClass = toasterClass,
                showToastMethodName = showToastMethodName,
            )
        }

    }

}

open class ToastPluginParameter(
    var isEnabled: Boolean = true,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet(),
    var toasterClass: String = "",
    var showToastMethodName: String = ""
)