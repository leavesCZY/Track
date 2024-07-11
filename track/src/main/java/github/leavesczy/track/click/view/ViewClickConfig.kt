package github.leavesczy.track.click.view

import java.io.Serializable

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal data class ViewClickConfig(
    val onClickClass: String,
    val onClickMethodName: String,
    val uncheckViewOnClickAnnotation: String,
    val include: Set<String>,
    val exclude: Set<String>,
    val hookPointList: Set<ViewClickHookPoint>
) : Serializable {

    companion object {

        operator fun invoke(pluginParameter: ViewClickPluginParameter): ViewClickConfig? {
            val onClickClass = pluginParameter.onClickClass
            val onClickMethodName = pluginParameter.onClickMethodName
            return if (onClickClass.isBlank() || onClickMethodName.isBlank()) {
                null
            } else {
                ViewClickConfig(
                    onClickClass = onClickClass,
                    onClickMethodName = onClickMethodName,
                    uncheckViewOnClickAnnotation = pluginParameter.uncheckViewOnClickAnnotation,
                    include = pluginParameter.include,
                    exclude = pluginParameter.exclude,
                    hookPointList = setOf(
                        ViewClickHookPoint(
                            interfaceName = "android/view/View\$OnClickListener",
                            methodName = "onClick",
                            methodNameWithDesc = "onClick(Landroid/view/View;)V"
                        )
                    )
                )
            }
        }

    }

}

internal data class ViewClickHookPoint(
    val interfaceName: String,
    val methodName: String,
    val methodNameWithDesc: String,
) : Serializable

open class ViewClickPluginParameter(
    var onClickClass: String = "",
    var onClickMethodName: String = "",
    var uncheckViewOnClickAnnotation: String = "",
    var include: Set<String> = setOf(),
    var exclude: Set<String> = setOf()
)