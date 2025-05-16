package github.leavesczy.track.click.view

import android.os.SystemClock
import android.util.Log
import android.view.View

/**
 * @Author: leavesCZY
 * @Date: 2025/5/16 11:43
 * @Desc:
 */
internal object ViewClickMonitor {

    private var lastClickTime = 0L

    private var clickIndex = 0

    @JvmStatic
    fun isEnabled(view: View): Boolean {
        clickIndex++
        val currentTime = SystemClock.elapsedRealtime()
        val isEnabled = currentTime - lastClickTime > 500L
        if (isEnabled) {
            lastClickTime = currentTime
        }
        log("onClick $clickIndex , isEnabled : $isEnabled")
        return isEnabled
    }

    private fun log(log: String) {
        Log.e(javaClass.simpleName, log)
    }

}