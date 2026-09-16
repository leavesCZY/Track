package github.leavesczy.track.click.view

import android.os.SystemClock
import android.util.Log
import android.view.View

internal object ViewClickHandler {

    private var lastClickTime = 0L

    private var clickIndex = 0

    @JvmStatic
    fun shouldHandleClick(view: View): Boolean {
        clickIndex++
        val currentTime = SystemClock.elapsedRealtime()
        val shouldHandle = currentTime - lastClickTime > 500L
        if (shouldHandle) {
            lastClickTime = currentTime
        }
        log("onClick $clickIndex , shouldHandleClick : $shouldHandle")
        return shouldHandle
    }

    private fun log(log: String) {
        Log.e(javaClass.simpleName, log)
    }

}
