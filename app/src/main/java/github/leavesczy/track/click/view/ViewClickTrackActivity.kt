package github.leavesczy.track.click.view

import android.os.Bundle
import android.view.View
import android.widget.TextView
import github.leavesczy.track.BaseActivity
import github.leavesczy.track.R

class ViewClickTrackActivity : BaseActivity() {

    private var clickIndex = 1

    @Suppress("ObjectLiteralToLambda")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_click_track)
        supportActionBar?.title = "ViewClickTrack"
        findViewById<TextView>(R.id.btnObjectSkip).setOnClickListener(object :
            View.OnClickListener {
            @SkipViewOnClick
            override fun onClick(view: View) {
                onClickView()
            }
        })
        findViewById<TextView>(R.id.btnObject).setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                onClickView()
            }
        })
        findViewById<TextView>(R.id.btnLambda).setOnClickListener {
            onClickView()
        }
    }

    fun onClickByXml(view: View) {
        onClickView()
    }

    private fun onClickView() {
        findViewById<TextView>(R.id.tvIndex).text = (clickIndex++).toString()
    }

}