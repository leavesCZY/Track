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
    val proxyOwner: String,
    val methods: List<String>
) : BaseTrackConfig {

    companion object {

        operator fun invoke(pluginParameter: ReplaceMethodPluginParameter): ReplaceMethodConfig? {
            val proxyOwner = pluginParameter.proxyOwner
            val methods = pluginParameter.methods.mapNotNull {
                val owner = it.owner
                val name = it.name
                val desc = it.desc
                if (owner.isBlank() || name.isBlank() || desc.isBlank()) {
                    null
                } else {
                    owner + name + desc
                }
            }
            if (proxyOwner.isBlank() || methods.isEmpty()) {
                return null
            }
            return ReplaceMethodConfig(
                isEnabled = pluginParameter.isEnabled,
                include = pluginParameter.include,
                exclude = pluginParameter.exclude,
                proxyOwner = proxyOwner,
                methods = methods
            )
        }

    }

}

open class ReplaceMethodPluginParameter(
    var isEnabled: Boolean = true,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet(),
    var proxyOwner: String = "",
    var methods: Set<MethodInstruction> = emptySet()
)

open class MethodInstruction(
    var owner: String,
    var name: String,
    var desc: String
)