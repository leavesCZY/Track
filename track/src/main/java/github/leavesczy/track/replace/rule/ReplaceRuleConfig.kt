package github.leavesczy.track.replace.rule

import github.leavesczy.track.BaseTrackConfig
import github.leavesczy.track.BaseTrackConfigParameters
import java.io.Serializable

internal data class ReplaceRuleConfig(
    override val include: Set<String>,
    override val exclude: Set<String>,
    override val extensionName: String,
    val replacements: Set<ReplaceRuleParameter>
) : BaseTrackConfig {

    data class ReplaceRuleParameter(
        val ownerClass: String,
        val memberName: String,
        val descriptor: String,
        val proxyClass: String
    ) : Serializable

}

internal interface ReplaceRuleConfigParameters :
    BaseTrackConfigParameters<ReplaceRuleConfig>

open class ReplaceFieldRule(
    var ownerClass: String,
    var fieldName: String,
    var typeDescriptor: String,
    var proxyClass: String
) {
    companion object {
        /** 匹配同名全部字段类型。 */
        const val MATCH_ALL_TYPE_DESCRIPTORS = "*"
    }
}

open class ReplaceMethodRule(
    var ownerClass: String,
    var methodName: String,
    var methodDescriptor: String,
    var proxyClass: String
) {
    companion object {
        /** 匹配同名全部方法重载。 */
        const val MATCH_ALL_METHOD_DESCRIPTORS = "*"
    }
}

open class ReplaceFieldTrackPluginParameter(
    var replacements: Set<ReplaceFieldRule> = emptySet(),
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet()
)

open class ReplaceMethodTrackPluginParameter(
    var replacements: Set<ReplaceMethodRule> = emptySet(),
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet()
)

open class ToastTrackPluginParameter(
    var proxyClass: String = "",
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet()
)

open class OptimizedThreadTrackPluginParameter(
    var proxyClass: String = "",
    var methodNames: Set<String> = setOf(),
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet()
)
