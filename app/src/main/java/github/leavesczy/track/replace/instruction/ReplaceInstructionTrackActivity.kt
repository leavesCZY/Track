package github.leavesczy.track.replace.instruction

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import github.leavesczy.track.R

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
@SuppressLint("MissingPermission", "HardwareIds", "NewApi", "SetTextI18n")
class ReplaceInstructionTrackActivity : AppCompatActivity() {

    companion object {

        var isProxyEnabled = true

    }

    private val btnProxyIsEnabled by lazy {
        findViewById<Button>(R.id.btnProxyIsEnabled)
    }

    private val btnTest by lazy {
        findViewById<Button>(R.id.btnTest)
    }

    private val tvLog by lazy {
        findViewById<TextView>(R.id.tvLog)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_replace_instruction_track)
        title = "Replace Field & Method Track"
        isProxyEnabled = true
        onProxyEnabledChanged()
        btnProxyIsEnabled.setOnClickListener {
            isProxyEnabled = !isProxyEnabled
            onProxyEnabledChanged()
        }
        btnTest.setOnClickListener {
            val log = buildString {
                append("DeviceId: " + getDeviceId(context = this@ReplaceInstructionTrackActivity))
                append("\n")
                append("Imei: " + getImei(context = this@ReplaceInstructionTrackActivity))
                append("\n")
                append("AndroidId: " + getAndroidId(context = this@ReplaceInstructionTrackActivity))
                append("\n")
                append("Brand: " + getBrand())
            }
            tvLog.append(log + "\n\n")
        }
    }

    private fun onProxyEnabledChanged() {
        FieldProxy.onProxyEnabledChanged()
        btnProxyIsEnabled.text = "ProxyIsEnabled : $isProxyEnabled"
    }

    private fun getDeviceId(context: Context): String {
        return try {
            val telephonyManager =
                context.getSystemService(android.app.Service.TELEPHONY_SERVICE) as? TelephonyManager
            telephonyManager?.deviceId ?: ""
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            ""
        }
    }

    private fun getImei(context: Context): String {
        return try {
            val telephonyManager =
                context.getSystemService(android.app.Service.TELEPHONY_SERVICE) as? TelephonyManager
            telephonyManager?.getImei(1) ?: ""
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            ""
        }
    }

    private fun getAndroidId(context: Context): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            ""
        }
    }

    private fun getBrand(): String {
        return Build.BRAND
    }

}