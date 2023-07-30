package github.leavesczy.trace

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import github.leavesczy.trace.click.compose.ComposeClickTraceActivity
import github.leavesczy.trace.click.view.ViewClickTraceActivity
import github.leavesczy.trace.replace.ReplaceClassTraceActivity
import github.leavesczy.trace.toast.ToastTraceActivity

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btnViewClick).setOnClickListener {
            startActivity<ViewClickTraceActivity>()
        }
        findViewById<View>(R.id.btnComposeClick).setOnClickListener {
            startActivity<ComposeClickTraceActivity>()
        }
        findViewById<View>(R.id.btnReplaceClass).setOnClickListener {
            startActivity<ReplaceClassTraceActivity>()
        }
        findViewById<View>(R.id.btnShowToast).setOnClickListener {
            startActivity<ToastTraceActivity>()
        }
    }

    private inline fun <reified T : Activity> startActivity() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }

}