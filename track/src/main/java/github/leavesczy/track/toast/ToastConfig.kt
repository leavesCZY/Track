package github.leavesczy.track.toast

import java.io.Serializable

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal data class ToastConfig(
    val toasterClass: String,
    val showToastMethodName: String
) : Serializable {

    companion object {

        operator fun invoke(pluginParameter: ToastPluginParameter): ToastConfig? {
            val toasterClass = pluginParameter.toasterClass
            val showToastMethodName = pluginParameter.showToastMethodName
            if (toasterClass.isBlank() || showToastMethodName.isBlank()) {
                return null
            }
            return ToastConfig(
                toasterClass = toasterClass,
                showToastMethodName = showToastMethodName
            )
        }

    }

}

open class ToastPluginParameter(
    var toasterClass: String = "",
    var showToastMethodName: String = ""
)