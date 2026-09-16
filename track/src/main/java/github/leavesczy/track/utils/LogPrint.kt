package github.leavesczy.track.utils

internal object LogPrint {

    fun normal(tag: String, msg: () -> String) {
        println(buildLog(tag = tag, msg = msg).normal)
    }

    fun error(tag: String, msg: () -> String) {
        println(buildLog(tag = tag, msg = msg).error)
    }

    private fun buildLog(tag: String, msg: () -> String): ColoredLog {
        return ColoredLog(text = "[${tag}]: " + msg())
    }

}

private class ColoredLog(val text: String) {

    val error: String
        get() = "\u001B[31m$text\u001B[0m"

    val normal: String
        get() = "\u001B[32m$text\u001B[0m"

}