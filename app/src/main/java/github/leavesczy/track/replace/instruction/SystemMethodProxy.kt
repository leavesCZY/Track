package github.leavesczy.track.replace.instruction

import android.content.ContentResolver
import android.provider.Settings
import android.telephony.TelephonyManager

internal object SystemMethodProxy {

    private val isProxyEnabled: Boolean
        get() = ReplaceInstructionTrackActivity.isProxyEnabled

    @JvmStatic
    fun getDeviceId(telephonyManager: TelephonyManager): String {
        return if (isProxyEnabled) {
            "这是一个假的 DeviceId"
        } else {
            try {
                telephonyManager.deviceId ?: ""
            } catch (e: Throwable) {
                e.message ?: ""
            }
        }
    }

    @JvmStatic
    fun getImei(telephonyManager: TelephonyManager, slotIndex: Int): String {
        return if (isProxyEnabled) {
            "这是一个假的 imei $slotIndex"
        } else {
            try {
                telephonyManager.getImei(slotIndex) ?: ""
            } catch (e: Throwable) {
                e.message ?: ""
            }
        }
    }

    @JvmStatic
    fun getString(resolver: ContentResolver, name: String): String {
        return if (isProxyEnabled) {
            "这是一个假的 AndroidId"
        } else {
            Settings.Secure.getString(resolver, name) ?: ""
        }
    }

}