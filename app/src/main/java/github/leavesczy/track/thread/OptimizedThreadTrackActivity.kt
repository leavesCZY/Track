package github.leavesczy.track.thread

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import github.leavesczy.track.R
import java.util.concurrent.Executors
import kotlin.random.Random

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
class OptimizedThreadTrackActivity : AppCompatActivity() {

    private val btnSubmitTask by lazy {
        findViewById<Button>(R.id.btnSubmitTask)
    }

    private val tvLog by lazy {
        findViewById<TextView>(R.id.tvLog)
    }

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
        setContentView(R.layout.activity_optimized_thread_track)
        title = "OptimizedThreadTrack"
        btnSubmitTask.setOnClickListener {
            newSingleThreadExecutor.execute {
                printThreadName("newSingleThreadExecutor")
            }
            newCachedThreadPool.execute {
                printThreadName("newCachedThreadPool")
            }
            newFixedThreadPool.execute {
                printThreadName("newFixedThreadPool")
            }
            newScheduledThreadPool.execute {
                printThreadName("newScheduledThreadPool")
            }
            newSingleThreadScheduledExecutor.execute {
                printThreadName("newSingleThreadScheduledExecutor")
            }
        }
    }

    private fun printThreadName(threadType: String) {
        Thread.sleep(Random.nextLong(100, 400))
        val threadName = Thread.currentThread().name
        runOnUiThread {
            tvLog.append("${threadType}: \n${threadName}")
            tvLog.append("\n\n")
        }
    }

}