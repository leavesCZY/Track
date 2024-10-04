package github.leavesczy.track

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import github.leavesczy.track.click.compose.ComposeClickTrackActivity
import github.leavesczy.track.click.view.ViewClickTrackActivity
import github.leavesczy.track.replace.clazz.ReplaceClassTrackActivity
import github.leavesczy.track.replace.instruction.ReplaceInstructionTrackActivity
import github.leavesczy.track.thread.OptimizedThreadTrackActivity
import github.leavesczy.track.toast.ToastTrackActivity

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btnViewClickTrack).setOnClickListener {
            startActivity<ViewClickTrackActivity>()
        }
        findViewById<View>(R.id.btnComposeClickTrack).setOnClickListener {
            startActivity<ComposeClickTrackActivity>()
        }
        findViewById<View>(R.id.btnToastTrack).setOnClickListener {
            startActivity<ToastTrackActivity>()
        }
        findViewById<View>(R.id.btnOptimizedThreadTrack).setOnClickListener {
            startActivity<OptimizedThreadTrackActivity>()
        }
        findViewById<View>(R.id.btnReplaceClassTrack).setOnClickListener {
            startActivity<ReplaceClassTrackActivity>()
        }
        findViewById<View>(R.id.btnReplaceInstructionTrack).setOnClickListener {
            startActivity<ReplaceInstructionTrackActivity>()
        }
    }

    private inline fun <reified T : Activity> startActivity() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }

}