package github.leavesczy.track.replace.method

import github.leavesczy.track.BaseTrackConfig
import java.io.Serializable

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal data class ReplaceMethodConfig(
    override val isEnabled: Boolean,
    override val include: Set<String>,
    override val exclude: Set<String>,
    val methods: Set<ReplaceMethodInstruction>
) : BaseTrackConfig {

    open class ReplaceMethodInstruction(
        val owner: String,
        val name: String,
        val descriptor: String,
        val proxyOwner: String
    ) : Serializable

    companion object {

        operator fun invoke(pluginParameter: ReplaceMethodPluginParameter): ReplaceMethodConfig? {
            val methods = pluginParameter.methods.mapNotNull {
                val owner = it.owner
                val name = it.name
                val desc = it.descriptor
                val proxyOwner = it.proxyOwner
                if (owner.isBlank() || name.isBlank() || desc.isBlank() || proxyOwner.isBlank()) {
                    null
                } else {
                    ReplaceMethodInstruction(
                        owner = owner,
                        name = name,
                        descriptor = desc,
                        proxyOwner = proxyOwner
                    )
                }
            }.toSet()
            if (methods.isEmpty()) {
                return null
            }
            return ReplaceMethodConfig(
                isEnabled = pluginParameter.isEnabled,
                include = pluginParameter.include,
                exclude = pluginParameter.exclude,
                methods = methods
            )
        }

    }

}

open class ReplaceMethodPluginParameter(
    var isEnabled: Boolean = true,
    var include: Set<String> = emptySet(),
    var exclude: Set<String> = emptySet(),
    var methods: Set<MethodInstruction> = emptySet()
)

open class MethodInstruction(
    var owner: String,
    var name: String,
    var descriptor: String,
    var proxyOwner: String
)