package github.leavesczy.trace.utils

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal object LogPrint {

    fun normal(tag: String, msg: () -> Any) {
        println(buildLog(tag = tag, msg = msg).normal)
    }

    fun error(tag: String, msg: () -> Any) {
        println(buildLog(tag = tag, msg = msg).error)
    }

    private fun buildLog(tag: String, msg: () -> Any): LogUI {
        return LogUI(text = "[${tag}]: " + msg())
    }

}

private class LogUI(val text: String) {

    val error: String
        get() = "\u001B[31m$text\u001B[0m"

    val normal: String
        get() = "\u001B[32m$text\u001B[0m"

}