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
    val toOwner: String,
    val fields: List<String>
) : BaseTrackConfig {

    companion object {

        private fun ReplaceFieldInstruction.mapInstruction(): String {
            if (fromOwner.isBlank() || fromName.isBlank() || fromDesc.isBlank()) {
                throw RuntimeException("ReplaceFieldTrack 传入了非法指令")
            }
            return fromOwner + fromName + fromDesc
        }

        operator fun invoke(pluginParameter: ReplaceFieldPluginParameter): ReplaceFieldConfig? {
            val toOwner = pluginParameter.toOwner
            val fields = pluginParameter.fields.map {
                it.mapInstruction()
            }
            if (toOwner.isBlank() || fields.isEmpty()) {
                return null
            }
            return ReplaceFieldConfig(
                isEnabled = pluginParameter.isEnabled,
                include = pluginParameter.include,
                exclude = pluginParameter.exclude,
                toOwner = toOwner,
                fields = fields
            )
        }

    }

}

open class ReplaceFieldPluginParameter(
    var isEnabled: Boolean = true,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet(),
    var toOwner: String = "",
    var fields: Set<ReplaceFieldInstruction> = emptySet()
)

open class ReplaceFieldInstruction(
    var fromOwner: String,
    var fromName: String,
    var fromDesc: String
)