package github.leavesczy.track.replace.rule

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.leavesczy.track.BaseActivity
import github.leavesczy.track.click.compose.TrackTheme
import github.leavesczy.track.click.compose.TrackTopAppBar

class ReplaceRuleTrackActivity : BaseActivity() {

    companion object {

        var isProxyEnabled = true

    }

    private var proxyEnabled by mutableStateOf(value = true)

    private var log by mutableStateOf(value = "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isProxyEnabled = true
        proxyEnabled = true
        SystemFieldProxy.onProxyEnabledChanged()
        setContent {
            TrackTheme {
                ReplaceRuleTrackScreen(
                    log = log,
                    proxyEnabled = proxyEnabled,
                    onOutputSystemValues = ::appendSystemValues,
                    onToggleProxyEnabled = ::toggleProxyEnabled
                )
            }
        }
    }

    private fun appendSystemValues() {
        val result = buildString {
            append("DeviceId: " + getDeviceId(context = this@ReplaceRuleTrackActivity))
            append("\n")
            append("imei: " + getImei(context = this@ReplaceRuleTrackActivity))
            append("\n")
            append("AndroidId: " + getAndroidId(context = this@ReplaceRuleTrackActivity))
            append("\n")
            append("Brand: " + getBrand())
        }
        log += result + "\n\n"
    }

    private fun toggleProxyEnabled() {
        isProxyEnabled = !isProxyEnabled
        proxyEnabled = isProxyEnabled
        SystemFieldProxy.onProxyEnabledChanged()
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceId(context: Context): String {
        return try {
            val telephonyManager =
                context.getSystemService(TELEPHONY_SERVICE) as? TelephonyManager
            telephonyManager?.deviceId ?: ""
        } catch (_: Throwable) {
            "ERROR"
        }
    }

    @SuppressLint("MissingPermission")
    private fun getImei(context: Context): String {
        return try {
            val telephonyManager =
                context.getSystemService(TELEPHONY_SERVICE) as? TelephonyManager
            telephonyManager?.getImei(1) ?: ""
        } catch (_: Throwable) {
            "ERROR"
        }
    }

    private fun getAndroidId(context: Context): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
        } catch (_: Throwable) {
            "ERROR"
        }
    }

    private fun getBrand(): String {
        return Build.BRAND
    }

}

@Composable
private fun ReplaceRuleTrackScreen(
    log: String,
    proxyEnabled: Boolean,
    onOutputSystemValues: () -> Unit,
    onToggleProxyEnabled: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TrackTopAppBar(title = "ReplaceRuleTrack")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOutputSystemValues
            ) {
                Text(text = "输出指定字段 & 指定方法的返回值")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onToggleProxyEnabled
            ) {
                Text(text = "是否替换 : $proxyEnabled")
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 5.dp)
                    .weight(weight = 1f)
                    .verticalScroll(state = rememberScrollState()),
                text = log,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
