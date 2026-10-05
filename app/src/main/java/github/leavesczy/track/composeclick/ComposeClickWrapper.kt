package github.leavesczy.track.composeclick

import android.os.SystemClock
import android.util.Log

/** clickWrapper 约定：构造接收 `Function0`，在 invoke 时做防抖。 */
internal class ComposeClickWrapper(private val onClick: () -> Unit) : Function0<Unit> {

    companion object {

        private var lastClickTime = 0L

    }

    override fun invoke() {
        val currentTime = SystemClock.elapsedRealtime()
        val shouldHandle = currentTime - lastClickTime > 500
        log("onClick shouldHandleClick : $shouldHandle")
        if (shouldHandle) {
            lastClickTime = currentTime
            onClick()
        }
    }

    private fun log(log: String) {
        Log.e(
            "ComposeClickWrapper",
            "${System.identityHashCode(this)} ${System.identityHashCode(onClick)} $log"
        )
    }

}
