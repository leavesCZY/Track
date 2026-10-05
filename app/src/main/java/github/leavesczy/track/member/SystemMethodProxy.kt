package github.leavesczy.track.member

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.Settings
import android.telephony.TelephonyManager

/** 实例方法改写为 INVOKESTATIC，receiver 作为第一参数，故使用 @JvmStatic。 */
@SuppressLint("MissingPermission", "HardwareIds", "NewApi")
internal object SystemMethodProxy {

    private val isProxyEnabled: Boolean
        get() = MemberTrackActivity.isProxyEnabled

    @JvmStatic
    fun getDeviceId(telephonyManager: TelephonyManager): String {
        return if (isProxyEnabled) {
            "这是一个假的 DeviceId"
        } else {
            try {
                telephonyManager.deviceId ?: ""
            } catch (e: Throwable) {
                "error"
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
                "error"
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
