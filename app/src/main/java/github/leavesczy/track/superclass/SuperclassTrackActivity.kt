package github.leavesczy.track.superclass

import android.os.Bundle
import github.leavesczy.track.BaseActivity
import github.leavesczy.track.R

class SuperclassTrackActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_superclass_track)
        supportActionBar?.title = "SuperclassTrack"
    }

}
