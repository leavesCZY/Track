package github.leavesczy.track

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val primary = ContextCompat.getColor(this, R.color.color_primary)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(scrim = primary),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        applySystemBarInsets()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        applySystemBarInsets()
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        applySystemBarInsets()
    }

    private fun applySystemBarInsets() {
        val actionBarContainer =
            findViewById<View>(androidx.appcompat.R.id.action_bar_container) ?: return
        val contentRoot =
            findViewById<ViewGroup>(android.R.id.content).getChildAt(0) ?: return
        val primary = ContextCompat.getColor(this, R.color.color_primary)
        actionBarContainer.setBackgroundColor(primary)
        findViewById<View>(androidx.appcompat.R.id.action_bar)?.setBackgroundColor(primary)
        supportActionBar?.elevation = 0f

        fun syncContentPadding() {
            val rootInsets = ViewCompat.getRootWindowInsets(contentRoot) ?: return
            val navigationBars = rootInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val contentLoc = IntArray(2)
            val barLoc = IntArray(2)
            contentRoot.getLocationInWindow(contentLoc)
            actionBarContainer.getLocationInWindow(barLoc)
            val overlap = (barLoc[1] + actionBarContainer.height - contentLoc[1]).coerceAtLeast(0)
            contentRoot.updatePadding(
                left = navigationBars.left,
                top = overlap,
                right = navigationBars.right,
                bottom = navigationBars.bottom
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(actionBarContainer) { view, windowInsets ->
            val statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBars.top)
            view.post { syncContentPadding() }
            windowInsets
        }
        ViewCompat.setOnApplyWindowInsetsListener(contentRoot) { _, windowInsets ->
            contentRoot.post { syncContentPadding() }
            windowInsets
        }
        actionBarContainer.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            syncContentPadding()
        }
        ViewCompat.requestApplyInsets(window.decorView)
    }

}