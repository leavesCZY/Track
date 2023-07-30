package github.leavesczy.trace

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

}