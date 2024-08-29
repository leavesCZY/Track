package github.leavesczy.track.click.compose

import github.leavesczy.track.BaseTrackConfig

internal data class ComposeClickConfig(
    override val isEnabled: Boolean,
    override val include: Set<String>,
    override val exclude: Set<String>,
    override val extensionName: String,
    val onClickClass: String,
    val onClickWhiteList: String
) : BaseTrackConfig {

    companion object {

        operator fun invoke(
            pluginParameter: ComposeClickPluginParameter,
            extensionName: String
        ): ComposeClickConfig? {
            val onClickClass = pluginParameter.onClickClass
            val onClickWhiteList = pluginParameter.onClickWhiteList
            return if (onClickClass.isBlank()) {
                null
            } else {
                ComposeClickConfig(
                    isEnabled = pluginParameter.isEnabled,
                    include = emptySet(),
                    exclude = emptySet(),
                    extensionName = extensionName,
                    onClickClass = onClickClass,
                    onClickWhiteList = onClickWhiteList
                )
            }
        }

    }

}

open class ComposeClickPluginParameter(
    var isEnabled: Boolean = true,
    var onClickClass: String = "",
    var onClickWhiteList: String = ""
)