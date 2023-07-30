package github.leavesczy.trace.click

import java.io.Serializable

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal data class ClickConfig(
    val viewClickConfig: ViewClickConfig?,
    val composeClickConfig: ComposeClickConfig?
) {

    companion object {

        operator fun invoke(pluginParameter: ClickPluginParameter): ClickConfig {
            val viewClickParameter = pluginParameter.view
            val viewOnClickClass = viewClickParameter.onClickClass
            val viewOnClickMethodName = viewClickParameter.onClickMethodName
            val viewClickConfig =
                if (viewOnClickClass.isBlank() || viewOnClickMethodName.isBlank()) {
                    null
                } else {
                    ViewClickConfig(
                        onClickClass = viewOnClickClass,
                        onClickMethodName = viewOnClickMethodName,
                        uncheckViewOnClickAnnotation = viewClickParameter.uncheckViewOnClickAnnotation,
                        include = viewClickParameter.include,
                        exclude = viewClickParameter.exclude,
                        hookPointList = listOf(
                            ViewClickHookPoint(
                                interfaceName = "android/view/View\$OnClickListener",
                                methodName = "onClick",
                                methodNameWithDesc = "onClick(Landroid/view/View;)V"
                            )
                        )
                    )
                }
            val composeClickParameter = pluginParameter.compose
            val composeOnClickClass = composeClickParameter.onClickClass
            val composeOnClickWhiteList = composeClickParameter.onClickWhiteList
            val composeClickConfig = if (composeOnClickClass.isBlank()) {
                null
            } else {
                ComposeClickConfig(
                    onClickClass = composeOnClickClass,
                    onClickWhiteList = composeOnClickWhiteList
                )
            }
            return ClickConfig(
                viewClickConfig = viewClickConfig,
                composeClickConfig = composeClickConfig
            )
        }

    }

}

internal data class ViewClickConfig(
    val onClickClass: String,
    val onClickMethodName: String,
    val uncheckViewOnClickAnnotation: String,
    val include: List<String>,
    val exclude: List<String>,
    val hookPointList: List<ViewClickHookPoint>
) : Serializable

internal data class ViewClickHookPoint(
    val interfaceName: String,
    val methodName: String,
    val methodNameWithDesc: String,
) : Serializable

internal data class ComposeClickConfig(
    val onClickClass: String,
    val onClickWhiteList: String
) : Serializable

open class ClickPluginParameter(
    var view: ViewClickParameter = ViewClickParameter(),
    var compose: ComposeClickParameter = ComposeClickParameter()
) {

    fun view(action: ViewClickParameter.() -> Unit) {
        view.action()
    }

    fun compose(action: ComposeClickParameter.() -> Unit) {
        compose.action()
    }

}

open class ViewClickParameter(
    var onClickClass: String = "",
    var onClickMethodName: String = "",
    var uncheckViewOnClickAnnotation: String = "",
    var include: List<String> = listOf(),
    var exclude: List<String> = listOf()
)

open class ComposeClickParameter(
    var onClickClass: String = "",
    var onClickWhiteList: String = ""
)