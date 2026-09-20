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
                    onShowOriginal = {
                        showOriginalToast()
                        buildMemberText(instrumented = false)
                    },
                    onShowInstrumented = {
                        showTrackedToast()
                        buildMemberText(instrumented = true)
                    }
                )
            }
        }
    }

    private fun showTrackedToast() {
        Toast.makeText(this, "原始 Toast 文案", Toast.LENGTH_SHORT).show()
    }

    private fun showOriginalToast() {
        MemberOutsideScope.showRawToast(context = this, message = "原始 Toast 文案")
    }

    @SuppressLint("HardwareIds")
    private fun buildMemberText(instrumented: Boolean): String {
        val brandInstrumented = Build.BRAND
        val brandOriginal = MemberOutsideScope.readBrand()
        val androidIdInstrumented =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: ""
        val androidIdOriginal = MemberOutsideScope.readAndroidId(context = this)
        val echoStringInstrumented = Echo.echo(value = "Track")
        val echoIntInstrumented = Echo.echo(value = 42)
        val echoStringOriginal = MemberOutsideScope.echoString(value = "Track")
        val echoIntOriginal = MemberOutsideScope.echoInt(value = 42)
        val modelInstrumented = DeviceInfo().model
        val modelOriginal = MemberOutsideScope.readModel()
        val title = if (instrumented) "【插桩后的值】" else "【原始值】"
        val brand = if (instrumented) brandInstrumented else brandOriginal
        val androidId = if (instrumented) androidIdInstrumented else androidIdOriginal
        val echoString = if (instrumented) echoStringInstrumented else echoStringOriginal
        val echoInt = if (instrumented) echoIntInstrumented else echoIntOriginal
        val model = if (instrumented) modelInstrumented else modelOriginal
        val toastLine = if (instrumented) {
            "Toast：已被 ToastProxy 接管（看弹出文案）"
        } else {
            "Toast：原始文案（看弹出文案）"
        }
        return """
            $title

            $toastLine

            Build.BRAND（GETSTATIC）
            · $brand

            DeviceInfo.model（GETFIELD）
            · $model

            Settings.Secure.getString(ANDROID_ID)
            · $androidId

            Echo.echo（methodDescriptor = "*"）
            · echo("Track") = $echoString
            · echo(42) = $echoInt
        """.trimIndent()
    }

}

@Composable
private fun MemberTrackScreen(
    onShowOriginal: () -> String,
    onShowInstrumented: () -> String
) {
    var result by remember { mutableStateOf(value = "点击下方按钮查看结果") }
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
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    result = onShowOriginal()
                }
            ) {
                Text(text = "原始值")
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    result = onShowInstrumented()
                }
            ) {
                Text(text = "插桩后的值")
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = result,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
