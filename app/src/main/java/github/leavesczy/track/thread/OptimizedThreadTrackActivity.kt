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

    private val btnAnonymousThread by lazy {
        findViewById<Button>(R.id.btnAnonymousThread)
    }

    private val tvLog by lazy {
        findViewById<TextView>(R.id.tvLog)
    }

    private val newFixedThreadPool = Executors.newFixedThreadPool(1)

    private val newSingleThreadExecutor = Executors.newSingleThreadExecutor()

    private val newCachedThreadPool = Executors.newCachedThreadPool()

    private val newSingleThreadScheduledExecutor = Executors.newSingleThreadScheduledExecutor()

    private val newScheduledThreadPool = Executors.newScheduledThreadPool(1) {
        val thread = Thread(it)
        thread.name = "newScheduledThreadPool"
        thread
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_optimized_thread_track)
        title = "OptimizedThreadTrack"
        btnSubmitTask.setOnClickListener {
            newFixedThreadPool.execute {
                printThreadName("newFixedThreadPool")
            }
            newSingleThreadExecutor.execute {
                printThreadName("newSingleThreadExecutor")
            }
            newCachedThreadPool.execute {
                printThreadName("newCachedThreadPool")
            }
            newSingleThreadScheduledExecutor.execute {
                printThreadName("newSingleThreadScheduledExecutor")
            }
            newScheduledThreadPool.execute {
                printThreadName("newScheduledThreadPool")
            }
        }
        btnAnonymousThread.setOnClickListener {
            Thread {
                printThreadName("newThread")
            }.start()
        }
    }

    private fun printThreadName(threadType: String) {
        Thread.sleep(Random.nextLong(400))
        val threadName = Thread.currentThread().name
        runOnUiThread {
            tvLog.append("\n\n${threadType}: \n${threadName}")
        }
    }

}