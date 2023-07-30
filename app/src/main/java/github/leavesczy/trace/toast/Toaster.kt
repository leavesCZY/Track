package github.leavesczy.trace.toast

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.Message
import android.widget.Toast

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
object Toaster {

    @JvmStatic
    fun showToast(toast: Toast) {
        hookToastIfNeed(toast)
        toast.setText("Toast 内容被修改了 ~")
        toast.show()
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun hookToastIfNeed(toast: Toast) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            try {
                val cToast = Toast::class.java
                val fTn = cToast.getDeclaredField("mTN")
                fTn.isAccessible = true
                val oTn = fTn.get(toast)
                val cTn = oTn.javaClass
                val fHandle = cTn.getDeclaredField("mHandler")
                fHandle.isAccessible = true
                fHandle.set(oTn, ProxyHandler(fHandle.get(oTn) as Handler))
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private class ProxyHandler(private val mHandler: Handler) : Handler(mHandler.looper) {

        override fun handleMessage(msg: Message) {
            try {
                mHandler.handleMessage(msg)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

    }

}