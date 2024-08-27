package github.leavesczy.track.replace.field

import github.leavesczy.track.BaseTrackConfig

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal data class ReplaceFieldConfig(
    override val isEnabled: Boolean,
    override val include: Set<String>,
    override val exclude: Set<String>,
    val proxyOwner: String,
    val fields: List<String>
) : BaseTrackConfig {

    companion object {

        operator fun invoke(pluginParameter: ReplaceFieldPluginParameter): ReplaceFieldConfig? {
            val proxyOwner = pluginParameter.proxyOwner
            val fields = pluginParameter.fields.mapNotNull {
                val owner = it.owner
                val name = it.name
                val desc = it.desc
                if (owner.isBlank() || name.isBlank() || desc.isBlank()) {
                    null
                } else {
                    owner + name + desc
                }
            }
            if (proxyOwner.isBlank() || fields.isEmpty()) {
                return null
            }
            return ReplaceFieldConfig(
                isEnabled = pluginParameter.isEnabled,
                include = pluginParameter.include,
                exclude = pluginParameter.exclude,
                proxyOwner = proxyOwner,
                fields = fields
            )
        }

    }

}

open class ReplaceFieldPluginParameter(
    var isEnabled: Boolean = true,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet(),
    var proxyOwner: String = "",
    var fields: Set<FieldInstruction> = emptySet()
)

open class FieldInstruction(
    var owner: String,
    var name: String,
    var desc: String
)