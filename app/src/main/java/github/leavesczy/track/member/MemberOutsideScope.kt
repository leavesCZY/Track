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

    fun showRawToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun echoString(value: String): String {
        return Echo.echo(value = value)
    }

    fun echoInt(value: Int): String {
        return Echo.echo(value = value)
    }

    fun readModel(): String {
        return DeviceInfo().model
    }

}
