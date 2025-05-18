package github.leavesczy.track.replace.clazz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import github.leavesczy.track.R

/**
 * @Author: leavesCZY
 * @Date: 2025/5/16 11:43
 * @Desc:
 */
class ReplaceClassTrackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_replace_class_track)
        supportActionBar?.title = "ReplaceClassTrack"
    }

}