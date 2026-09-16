package github.leavesczy.track.click.compose

import android.os.SystemClock
import android.util.Log

class ComposeClickWrapper(private val onClick: () -> Unit) : Function0<Unit> {

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
