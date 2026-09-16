package github.leavesczy.track.replace.inheritance

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
import github.leavesczy.track.click.compose.TrackTheme
import github.leavesczy.track.click.compose.TrackTopAppBar

class ReplaceClassTrackActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrackTheme {
                ReplaceClassTrackScreen()
            }
        }
    }

}

@Composable
private fun ReplaceClassTrackScreen() {
    var name by remember { mutableStateOf(value = "Track") }
    var result by remember { mutableStateOf(value = "") }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TrackTopAppBar(title = "ReplaceClassTrack")
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
                text = "OriginGreeter → ProxyGreeter。AppGreeter 应被替换，ExcludedGreeter 被 exclude。",
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
                    result = buildReplaceClassResult(name = name.ifBlank { "Track" })
                }
            ) {
                Text(text = "调用 greet() 并检查继承关系")
            }
            Text(text = "结果", fontSize = 16.sp)
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = result.ifBlank {
                    "点击上方按钮查看：父类是否换成 ProxyGreeter，以及 super.greet() 实际落到谁"
                },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun buildReplaceClassResult(name: String): String {
    val appGreeter = AppGreeter()
    val excludedGreeter = ExcludedGreeter()
    return buildString {
        appendLine("【AppGreeter — 应被替换】")
        appendLine("class = ${appGreeter.javaClass.name}")
        appendLine("super = ${appGreeter.javaClass.superclass?.name}")
        appendLine("super is ProxyGreeter = ${appGreeter.javaClass.superclass == ProxyGreeter::class.java}")
        appendLine("greet() = ${appGreeter.greet(name)}")
        appendLine()
        appendLine("【ExcludedGreeter — exclude，应保持原父类】")
        appendLine("class = ${excludedGreeter.javaClass.name}")
        appendLine("super = ${excludedGreeter.javaClass.superclass?.name}")
        appendLine("super is ProxyGreeter = ${excludedGreeter.javaClass.superclass == ProxyGreeter::class.java}")
        append("greet() = ${excludedGreeter.greet(name)}")
    }
}
