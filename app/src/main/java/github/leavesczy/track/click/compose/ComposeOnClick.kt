package github.leavesczy.track.click.compose

import android.os.SystemClock
import android.util.Log

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
class ComposeOnClick(private val onClick: () -> Unit) : Function0<Unit> {

    companion object {

        private var lastClickTime = 0L

    }

    override fun invoke() {
        val currentTime = SystemClock.elapsedRealtime()
        val isEnabled = currentTime - lastClickTime > 500
        log("onClick isEnabled : $isEnabled")
        if (isEnabled) {
            lastClickTime = currentTime
            onClick()
        }
    }

    private fun log(log: String) {
        Log.e(
            "ComposeOnClick",
            "${System.identityHashCode(this)} ${System.identityHashCode(onClick)} $log"
        )
    }

}