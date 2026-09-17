package github.leavesczy.track.member

/**
 * 用于演示 methodDescriptor = "*"：同名全部重载都会替换。
 */
internal object Echo {

    @JvmStatic
    fun echo(value: String): String {
        return "origin-echo:$value"
    }

    @JvmStatic
    fun echo(value: Int): String {
        return "origin-echo:$value"
    }

}

internal object EchoProxy {

    @JvmStatic
    fun echo(value: String): String {
        return "proxy-echo:$value"
    }

    @JvmStatic
    fun echo(value: Int): String {
        return "proxy-echo:$value"
    }

}
