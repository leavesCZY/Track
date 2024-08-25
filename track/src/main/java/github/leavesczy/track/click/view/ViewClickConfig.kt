package github.leavesczy.track.click.view

import github.leavesczy.track.BaseTrackConfig
import java.io.Serializable

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal data class ViewClickConfig(
    override val isEnabled: Boolean,
    override val include: Set<String>,
    override val exclude: Set<String>,
    val onClickClass: String,
    val onClickMethodName: String,
    val uncheckViewOnClickAnnotation: String,
    val hookPointList: Set<ViewClickHookPoint>
) : BaseTrackConfig {

    companion object {

        operator fun invoke(pluginParameter: ViewClickPluginParameter): ViewClickConfig? {
            val onClickClass = pluginParameter.onClickClass
            val onClickMethodName = pluginParameter.onClickMethodName
            return if (onClickClass.isBlank() || onClickMethodName.isBlank()) {
                null
            } else {
                ViewClickConfig(
                    isEnabled = pluginParameter.isEnabled,
                    include = pluginParameter.include,
                    exclude = pluginParameter.exclude,
                    onClickClass = onClickClass,
                    onClickMethodName = onClickMethodName,
                    uncheckViewOnClickAnnotation = pluginParameter.uncheckViewOnClickAnnotation,
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
    var isEnabled: Boolean = true,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet(),
    var onClickClass: String = "",
    var onClickMethodName: String = "",
    var uncheckViewOnClickAnnotation: String = ""
)