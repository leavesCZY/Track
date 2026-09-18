package github.leavesczy.track.member

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.widget.Toast

/**
 * 不在 memberTrack include 范围内：同名系统调用应保持原样，用于对照 MemberTrackActivity。
 */
internal object MemberOutsideScope {

    fun readBrand(): String {
        return Build.BRAND
    }

    @SuppressLint("HardwareIds")
    fun readAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
    }

    fun showToast(context: Context) {
        Toast.makeText(context, "OutsideScope 原始 Toast", Toast.LENGTH_SHORT).show()
    }

}
