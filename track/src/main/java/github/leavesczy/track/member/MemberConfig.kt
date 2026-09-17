package github.leavesczy.track.member

import github.leavesczy.track.BaseTrackConfig
import github.leavesczy.track.BaseTrackConfigParameters
import java.io.Serializable

/** 匹配全部 descriptor（字段类型或方法重载）。 */
const val MATCH_ALL_DESCRIPTORS = "*"

internal enum class MemberKind {
    FIELD,
    METHOD
}

internal data class MemberConfig(
    override val include: Set<String>,
    override val exclude: Set<String>,
    val replacements: Set<MemberReplacement>
) : BaseTrackConfig {

    data class MemberReplacement(
        val kind: MemberKind,
        val ownerClass: String,
        val memberName: String,
        val descriptor: String,
        val proxyClass: String
    ) : Serializable

}

internal interface MemberConfigParameters :
    BaseTrackConfigParameters<MemberConfig>

open class MemberFieldRule(
    var ownerClass: String,
    var fieldName: String,
    var typeDescriptor: String,
    var proxyClass: String,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet()
) {
    companion object {
        /** 匹配同名全部字段类型。 */
        const val MATCH_ALL_TYPE_DESCRIPTORS = MATCH_ALL_DESCRIPTORS
    }
}

open class MemberMethodRule(
    var ownerClass: String,
    var methodName: String,
    var methodDescriptor: String,
    var proxyClass: String,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet()
) {
    companion object {
        /** 匹配同名全部方法重载。 */
        const val MATCH_ALL_METHOD_DESCRIPTORS = MATCH_ALL_DESCRIPTORS
    }
}

open class MemberTrackPluginParameter(
    var methods: Set<MemberMethodRule> = emptySet(),
    var fields: Set<MemberFieldRule> = emptySet()
)
