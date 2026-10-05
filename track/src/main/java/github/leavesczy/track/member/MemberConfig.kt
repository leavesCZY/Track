package github.leavesczy.track.member

import github.leavesczy.track.BaseTrackConfig
import github.leavesczy.track.BaseTrackConfigParameters
import github.leavesczy.track.utils.replacePeriodWithSlash
import java.io.Serializable

/** 匹配全部 descriptor（字段类型或方法重载）。 */
internal const val MATCH_ALL_DESCRIPTORS = "*"

internal enum class MemberKind {
    FIELD,
    METHOD
}

internal data class MemberConfig(
    override val include: Set<String>,
    override val exclude: Set<String>,
    val replacements: Set<MemberReplacement>
) : BaseTrackConfig {

    val proxyClasses: Set<String> = replacements.mapTo(destination = mutableSetOf()) { it.proxyClass }

    /**
     * @param ownerClass ASM 内部名（斜杠分隔）
     * @param proxyClass 点分全限定名；[proxyOwner] 为对应内部名
     */
    data class MemberReplacement(
        val kind: MemberKind,
        val ownerClass: String,
        val memberName: String,
        val descriptor: String,
        val proxyClass: String
    ) : Serializable {

        val proxyOwner: String = replacePeriodWithSlash(className = proxyClass)

    }

}

internal interface MemberConfigParameters :
    BaseTrackConfigParameters<MemberConfig>

/** 字段读替换规则；[typeDescriptor] 可用 [MATCH_ALL_TYPE_DESCRIPTORS]。 */
data class MemberFieldRule(
    var ownerClass: String,
    var fieldName: String,
    var typeDescriptor: String,
    var proxyClass: String,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet()
) {
    companion object {
        const val MATCH_ALL_TYPE_DESCRIPTORS = MATCH_ALL_DESCRIPTORS
    }
}

/** 方法调用替换规则；[methodDescriptor] 可用 [MATCH_ALL_METHOD_DESCRIPTORS]。 */
data class MemberMethodRule(
    var ownerClass: String,
    var methodName: String,
    var methodDescriptor: String,
    var proxyClass: String,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet()
) {
    companion object {
        const val MATCH_ALL_METHOD_DESCRIPTORS = MATCH_ALL_DESCRIPTORS
    }
}

open class MemberTrackPluginParameter(
    var methods: Set<MemberMethodRule> = emptySet(),
    var fields: Set<MemberFieldRule> = emptySet()
)
