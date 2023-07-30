package github.leavesczy.trace.replace

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import github.leavesczy.trace.R

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
class ReplaceClassTraceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_replace_class_trace)
        title = "ReplaceClassTrace"
    }

}