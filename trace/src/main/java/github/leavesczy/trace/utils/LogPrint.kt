package github.leavesczy.trace.utils

import java.util.concurrent.Executors

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal object LogPrint {

    private val logThreadExecutor = Executors.newSingleThreadExecutor()

    fun normal(msg: () -> Any) {
        logThreadExecutor.submit {
            try {
                println("${LogUI.Normal.value}[Trace]: ${msg()}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun error(msg: () -> Any) {
        logThreadExecutor.submit {
            try {
                println("${LogUI.Error.value}[Trace]: ${msg()}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}

private enum class LogUI(val value: String) {
    Error("\u001B[0;31;40m"),
    Warn("\u001B[0;33;40m"),
    Info("\u001B[0;32;340m"),
    Normal("\u001B[0m")
}