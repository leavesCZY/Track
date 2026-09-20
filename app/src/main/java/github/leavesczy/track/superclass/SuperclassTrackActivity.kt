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
    var result by remember { mutableStateOf(value = "点击下方按钮查看结果") }
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
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
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = { result = buildSuperclassText(instrumented = false) }
            ) {
                Text(text = "原始值")
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = { result = buildSuperclassText(instrumented = true) }
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

private fun buildSuperclassText(instrumented: Boolean): String {
    val name = "Track"
    val appGreeter = AppGreeter()
    val excludedGreeter = ExcludedGreeter()
    val appLogger = AppLogger()
    val outsideLogger = OutsideLogger()
    val title = if (instrumented) "【插桩后的值】" else "【原始值】"
    val appGreeterSuper = if (instrumented) {
        appGreeter.javaClass.superclass?.simpleName.orEmpty()
    } else {
        OriginGreeter::class.java.simpleName
    }
    val appGreeterResult = if (instrumented) {
        appGreeter.greet(name)
    } else {
        "${OriginGreeter().greet(name)} (via AppGreeter)"
    }
    val appLoggerSuper = if (instrumented) {
        appLogger.javaClass.superclass?.simpleName.orEmpty()
    } else {
        OriginLogger::class.java.simpleName
    }
    val appLoggerResult = if (instrumented) {
        appLogger.log(message = name)
    } else {
        "${OriginLogger().log(message = name)} (via AppLogger)"
    }
    return """
        $title

        OriginGreeter → ProxyGreeter（exclude ExcludedGreeter）
        · AppGreeter
          父类：$appGreeterSuper
          结果：$appGreeterResult
        · ExcludedGreeter
          父类：${excludedGreeter.javaClass.superclass?.simpleName}
          结果：${excludedGreeter.greet(name)}

        OriginLogger → ProxyLogger（仅 include AppLogger）
        · AppLogger
          父类：$appLoggerSuper
          结果：$appLoggerResult
        · OutsideLogger
          父类：${outsideLogger.javaClass.superclass?.simpleName}
          结果：${outsideLogger.log(message = name)}
    """.trimIndent()
}
