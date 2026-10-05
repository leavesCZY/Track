package github.leavesczy.track.member

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.leavesczy.track.BaseActivity
import github.leavesczy.track.ui.TrackTheme
import github.leavesczy.track.ui.TrackTopAppBar

class MemberTrackActivity : BaseActivity() {

    companion object {

        var isProxyEnabled = true

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isProxyEnabled = true
        SystemFieldProxy.onProxyEnabledChanged()
        setContent {
            TrackTheme {
                MemberTrackScreen(
                    onToggleProxy = {
                        isProxyEnabled = !isProxyEnabled
                        SystemFieldProxy.onProxyEnabledChanged()
                        isProxyEnabled
                    },
                    onReadInstructions = {
                        buildInstructionLog()
                    }
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceId(context: Context): String {
        return try {
            val telephonyManager =
                context.getSystemService(TELEPHONY_SERVICE) as? TelephonyManager
            telephonyManager?.deviceId ?: ""
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            ""
        }
    }

    @SuppressLint("MissingPermission")
    private fun getImei(context: Context): String {
        return try {
            val telephonyManager =
                context.getSystemService(TELEPHONY_SERVICE) as? TelephonyManager
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

    private fun buildInstructionLog(): String {
        return buildString {
            append("DeviceId: " + getDeviceId(context = this@MemberTrackActivity))
            append("\n")
            append("Imei: " + getImei(context = this@MemberTrackActivity))
            append("\n")
            append("AndroidId: " + getAndroidId(context = this@MemberTrackActivity))
            append("\n")
            append("Brand: " + getBrand())
        }
    }

}

@Composable
private fun MemberTrackScreen(
    onToggleProxy: () -> Boolean,
    onReadInstructions: () -> String
) {
    var proxyEnabled by remember {
        mutableStateOf(value = true)
    }
    var log by remember {
        mutableStateOf(value = "")
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TrackTopAppBar(title = "MemberTrack")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(state = rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    log = if (log.isEmpty()) {
                        onReadInstructions()
                    } else {
                        log + "\n\n" + onReadInstructions()
                    }
                }
            ) {
                Text(text = "输出指定字段 & 指定方法的返回值")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    proxyEnabled = onToggleProxy()
                }
            ) {
                Text(text = "是否替换 : $proxyEnabled")
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = log,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
