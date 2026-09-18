package github.leavesczy.track.member

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrackTheme {
                MemberTrackScreen(
                    onShowToastInScope = ::showTrackedToast,
                    onShowToastOutsideScope = { MemberOutsideScope.showToast(context = this) },
                    onCompareMembers = ::buildMemberCompareResult
                )
            }
        }
    }

    private fun showTrackedToast() {
        Toast.makeText(this, "原始 Toast 文案", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("HardwareIds")
    private fun buildMemberCompareResult(): String {
        val brandInScope = Build.BRAND
        val brandOutside = MemberOutsideScope.readBrand()
        val androidIdInScope =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: ""
        val androidIdOutside = MemberOutsideScope.readAndroidId(context = this)
        val echoString = Echo.echo(value = "Track")
        val echoInt = Echo.echo(value = 42)
        return buildString {
            appendLine("【include 对照：仅 MemberTrackActivity 会被改写】")
            appendLine("Activity Build.BRAND = $brandInScope")
            appendLine("命中 Proxy = ${brandInScope == SystemFieldProxy.BRAND}")
            appendLine("OutsideScope Build.BRAND = $brandOutside")
            appendLine("Outside 保持原样 = ${brandOutside != SystemFieldProxy.BRAND}")
            appendLine()
            appendLine("Activity AndroidId = $androidIdInScope")
            appendLine("命中 Proxy = ${androidIdInScope == "proxy-android-id"}")
            appendLine("OutsideScope AndroidId = $androidIdOutside")
            appendLine("Outside 保持原样 = ${androidIdOutside != "proxy-android-id"}")
            appendLine()
            appendLine("【MATCH_ALL：Echo.echo 全部重载】")
            appendLine("echo(\"Track\") = $echoString")
            appendLine("命中 Proxy = ${echoString.startsWith("proxy-echo:")}")
            appendLine("echo(42) = $echoInt")
            append("命中 Proxy = ${echoInt.startsWith("proxy-echo:")}")
        }
    }

}

@Composable
private fun MemberTrackScreen(
    onShowToastInScope: () -> Unit,
    onShowToastOutsideScope: () -> Unit,
    onCompareMembers: () -> String
) {
    var result by remember { mutableStateOf(value = "") }
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
            Text(
                text = "include 仅覆盖 MemberTrackActivity；OutsideScope 同名调用应保持原样。Echo.echo 用 \"*\" 匹配全部重载。",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onShowToastInScope
            ) {
                Text(text = "Toast.show（Activity，应被改写）")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onShowToastOutsideScope
            ) {
                Text(text = "Toast.show（OutsideScope，应保持原样）")
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { result = onCompareMembers() }
            ) {
                Text(text = "对照 include / MATCH_ALL")
            }
            Text(text = "结果", fontSize = 16.sp)
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = result.ifBlank {
                    "先分别点两个 Toast 对比文案；再点对照按钮查看字段、静态方法与 Echo 重载是否符合预期"
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
