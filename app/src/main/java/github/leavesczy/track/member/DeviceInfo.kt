package github.leavesczy.track.member

/**
 * 用于演示 GETFIELD：实例字段读会改成 INVOKESTATIC proxy.model(receiver)。
 */
internal class DeviceInfo {

    @JvmField
    val model: String = "origin-model"

}

internal object DeviceInfoProxy {

    @JvmStatic
    fun model(receiver: DeviceInfo): String {
        return "proxy-model"
    }

}