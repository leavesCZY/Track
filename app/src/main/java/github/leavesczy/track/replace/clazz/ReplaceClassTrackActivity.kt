package github.leavesczy.track.replace.clazz

import android.os.Bundle
import github.leavesczy.track.BaseActivity
import github.leavesczy.track.R

class ReplaceClassTrackActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_replace_class_track)
        supportActionBar?.title = "ReplaceClassTrack"
    }

}