package github.leavesczy.track.replace.field

import github.leavesczy.track.BaseTrackConfig
import java.io.Serializable

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal data class ReplaceFieldConfig(
    override val isEnabled: Boolean,
    override val include: Set<String>,
    override val exclude: Set<String>,
    val fields: Set<ReplaceFieldInstruction>
) : BaseTrackConfig {

    open class ReplaceFieldInstruction(
        val owner: String,
        val name: String,
        val descriptor: String,
        val proxyOwner: String
    ) : Serializable

    companion object {

        operator fun invoke(pluginParameter: ReplaceFieldPluginParameter): ReplaceFieldConfig? {
            val fields = pluginParameter.fields.mapNotNull {
                val owner = it.owner
                val name = it.name
                val desc = it.descriptor
                val proxyOwner = it.proxyOwner
                if (owner.isBlank() || name.isBlank() || desc.isBlank() || proxyOwner.isBlank()) {
                    null
                } else {
                    ReplaceFieldInstruction(
                        owner = owner,
                        name = name,
                        descriptor = desc,
                        proxyOwner = proxyOwner
                    )
                }
            }.toSet()
            if (fields.isEmpty()) {
                return null
            }
            return ReplaceFieldConfig(
                isEnabled = pluginParameter.isEnabled,
                include = pluginParameter.include,
                exclude = pluginParameter.exclude,
                fields = fields
            )
        }

    }

}

open class ReplaceFieldPluginParameter(
    var isEnabled: Boolean = true,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet(),
    var fields: Set<FieldInstruction> = emptySet()
)

open class FieldInstruction(
    var owner: String,
    var name: String,
    var descriptor: String,
    var proxyOwner: String
)