package github.leavesczy.trace.click.compose

import java.io.Serializable

internal data class ComposeClickConfig(
    val onClickClass: String,
    val onClickWhiteList: String
) : Serializable {

    companion object {

        operator fun invoke(pluginParameter: ComposeClickPluginParameter): ComposeClickConfig? {
            val onClickClass = pluginParameter.onClickClass
            val onClickWhiteList = pluginParameter.onClickWhiteList
            return if (onClickClass.isBlank()) {
                null
            } else {
                ComposeClickConfig(
                    onClickClass = onClickClass,
                    onClickWhiteList = onClickWhiteList
                )
            }
        }

    }

}

open class ComposeClickPluginParameter(
    var onClickClass: String = "",
    var onClickWhiteList: String = ""
)