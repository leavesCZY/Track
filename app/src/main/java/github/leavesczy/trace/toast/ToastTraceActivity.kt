package github.leavesczy.trace.toast

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import github.leavesczy.trace.R

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
class ToastTraceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toast_trace)
        title = "ToastTrace"
        findViewById<View>(R.id.btnShowToast).setOnClickListener {
            Toast.makeText(this, "ToastTrace", Toast.LENGTH_SHORT).show()
        }
    }

}