package github.leavesczy.track.replace.method

import github.leavesczy.track.BaseTrackConfig

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal data class ReplaceMethodConfig(
    override val isEnabled: Boolean,
    override val include: Set<String>,
    override val exclude: Set<String>,
    val toOwner: String,
    val methods: List<String>
) : BaseTrackConfig {

    companion object {

        private fun ReplaceMethod.mapInstruction(): String {
            if (fromOwner.isBlank() || fromName.isBlank() || fromDesc.isBlank()) {
                throw RuntimeException("ReplaceMethodTrack 传入了非法指令")
            }
            return fromOwner + fromName + fromDesc
        }

        operator fun invoke(pluginParameter: ReplaceMethodPluginParameter): ReplaceMethodConfig? {
            val toOwner = pluginParameter.toOwner
            val methods = pluginParameter.methods.map {
                it.mapInstruction()
            }
            if (toOwner.isBlank() || methods.isEmpty()) {
                return null
            }
            return ReplaceMethodConfig(
                isEnabled = pluginParameter.isEnabled,
                include = pluginParameter.include,
                exclude = pluginParameter.exclude,
                toOwner = toOwner,
                methods = methods
            )
        }

    }

}

open class ReplaceMethodPluginParameter(
    var isEnabled: Boolean = true,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet(),
    var toOwner: String = "",
    var methods: Set<ReplaceMethod> = emptySet()
)

open class ReplaceMethod(
    var fromOwner: String,
    var fromName: String,
    var fromDesc: String
)