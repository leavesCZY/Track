package github.leavesczy.track.toast

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import github.leavesczy.track.R

/**
 * @Author: leavesCZY
 * @Date: 2025/5/16 11:43
 * @Desc:
 */
class ToastTrackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toast_track)
        supportActionBar?.title = "ToastTrack"
        findViewById<View>(R.id.btnToastTrack).setOnClickListener {
            Toast.makeText(this, "ToastTrack", Toast.LENGTH_SHORT).show()
        }
    }

}