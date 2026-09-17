package github.leavesczy.track.member

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.Message
import android.widget.Toast

internal object ToastProxy {

    @JvmStatic
    fun show(toast: Toast) {
        hookToastIfNeed(toast)
        toast.setText("Toast.show 已被 ToastProxy 接管")
        toast.show()
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun hookToastIfNeed(toast: Toast) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            try {
                val toastClass = Toast::class.java
                val tnField = toastClass.getDeclaredField("mTN")
                tnField.isAccessible = true
                val tn = tnField.get(toast)
                val handlerField = tn.javaClass.getDeclaredField("mHandler")
                handlerField.isAccessible = true
                handlerField.set(tn, ProxyHandler(handlerField.get(tn) as Handler))
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private class ProxyHandler(private val handler: Handler) : Handler(handler.looper) {

        override fun handleMessage(msg: Message) {
            try {
                handler.handleMessage(msg)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

    }

}
