package github.leavesczy.track.superclass

import android.os.Bundle
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
import androidx.compose.material3.OutlinedTextField
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

class SuperclassTrackActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrackTheme {
                SuperclassTrackScreen()
            }
        }
    }

}

@Composable
private fun SuperclassTrackScreen() {
    var name by remember { mutableStateOf(value = "Track") }
    var result by remember { mutableStateOf(value = "") }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TrackTopAppBar(title = "SuperclassTrack")
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
                text = "规则 1 用 exclude 拦住 ExcludedGreeter；规则 2 用 include 只放行 AppLogger（OutsideLogger 应保持原父类）。",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text(text = "name") }
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    result = buildSuperclassResult(name = name.ifBlank { "Track" })
                }
            ) {
                Text(text = "检查 exclude / include")
            }
            Text(text = "结果", fontSize = 16.sp)
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = result.ifBlank {
                    "点击上方按钮查看：exclude 与 include 是否分别拦住 ExcludedGreeter / OutsideLogger"
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun buildSuperclassResult(name: String): String {
    val appGreeter = AppGreeter()
    val excludedGreeter = ExcludedGreeter()
    val appLogger = AppLogger()
    val outsideLogger = OutsideLogger()
    return buildString {
        appendLine("【规则 1：exclude】OriginGreeter → ProxyGreeter")
        appendLine("AppGreeter.super = ${appGreeter.javaClass.superclass?.name}")
        appendLine("命中 ProxyGreeter = ${appGreeter.javaClass.superclass == ProxyGreeter::class.java}")
        appendLine("greet() = ${appGreeter.greet(name)}")
        appendLine()
        appendLine("ExcludedGreeter.super = ${excludedGreeter.javaClass.superclass?.name}")
        appendLine("仍为 OriginGreeter = ${excludedGreeter.javaClass.superclass == OriginGreeter::class.java}")
        appendLine("greet() = ${excludedGreeter.greet(name)}")
        appendLine()
        appendLine("【规则 2：include】OriginLogger → ProxyLogger")
        appendLine("AppLogger.super = ${appLogger.javaClass.superclass?.name}")
        appendLine("命中 ProxyLogger = ${appLogger.javaClass.superclass == ProxyLogger::class.java}")
        appendLine("log() = ${appLogger.log(message = name)}")
        appendLine()
        appendLine("OutsideLogger.super = ${outsideLogger.javaClass.superclass?.name}")
        appendLine("仍为 OriginLogger = ${outsideLogger.javaClass.superclass == OriginLogger::class.java}")
        append("log() = ${outsideLogger.log(message = name)}")
    }
}
