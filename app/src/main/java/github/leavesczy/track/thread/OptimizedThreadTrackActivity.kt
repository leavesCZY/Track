package github.leavesczy.track.thread

import android.os.Bundle
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
import java.util.concurrent.Executors
import kotlin.random.Random

class OptimizedThreadTrackActivity : BaseActivity() {

    private var log by mutableStateOf(value = "")

    private val newSingleThreadExecutor = Executors.newSingleThreadExecutor()

    private val newCachedThreadPool = Executors.newCachedThreadPool()

    private val newFixedThreadPool = Executors.newFixedThreadPool(1)

    private val newScheduledThreadPool = Executors.newScheduledThreadPool(1) {
        val thread = Thread(it)
        thread.name = "newScheduledThreadPool"
        thread
    }

    private val newSingleThreadScheduledExecutor = Executors.newSingleThreadScheduledExecutor {
        val thread = Thread(it)
        thread.name = "newSingleThreadScheduledExecutor"
        thread
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrackTheme {
                OptimizedThreadTrackScreen(
                    log = log,
                    onSubmitTask = ::submitTasks
                )
            }
        }
    }

    private fun submitTasks() {
        newSingleThreadExecutor.execute {
            printThreadName(threadType = "newSingleThreadExecutor")
        }
        newCachedThreadPool.execute {
            printThreadName(threadType = "newCachedThreadPool")
        }
        newFixedThreadPool.execute {
            printThreadName(threadType = "newFixedThreadPool")
        }
        newScheduledThreadPool.execute {
            printThreadName(threadType = "newScheduledThreadPool")
        }
        newSingleThreadScheduledExecutor.execute {
            printThreadName(threadType = "newSingleThreadScheduledExecutor")
        }
    }

    private fun printThreadName(threadType: String) {
        Thread.sleep(Random.nextLong(100, 400))
        val threadName = Thread.currentThread().name
        runOnUiThread {
            log += "${threadType}: \n${threadName}\n"
        }
    }

}

@Composable
private fun OptimizedThreadTrackScreen(
    log: String,
    onSubmitTask: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TrackTopAppBar(title = "OptimizedThreadTrack")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = onSubmitTask
            ) {
                Text(text = "向线程池提交任务")
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f)
                    .verticalScroll(state = rememberScrollState()),
                text = log,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
