package github.leavesczy.track

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * @Author: leavesCZY
 * @Date: 2025/9/12 17:15
 * @Desc:
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    protected fun setOnApplyWindowInsetsListener() {
        val rootView = findViewById<View>(R.id.rootView)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            run {
                val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
                val statusBarBackgroundView = findViewById<View>(R.id.viewStatusBar)
                statusBarBackgroundView.layoutParams.height = statusBarInsets.top
                statusBarBackgroundView.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.color_top_bar
                    )
                )
            }
            run {
                val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                view.setPadding(
                    navigationBars.left,
                    navigationBars.top,
                    navigationBars.right,
                    navigationBars.bottom
                )
            }
            insets
        }
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false
        windowInsetsController.isAppearanceLightNavigationBars = false
    }

}