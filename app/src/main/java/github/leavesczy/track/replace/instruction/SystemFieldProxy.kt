package github.leavesczy.track.replace.instruction

import android.os.Build

/**
 * @Author: leavesCZY
 * @Github: https://github.com/leavesCZY
 * @Desc:
 */
internal object SystemFieldProxy {

    @JvmField
    var BRAND = "这是一个假的 BRAND"

    fun onProxyEnabledChanged() {
        BRAND = if (ReplaceInstructionTrackActivity.isProxyEnabled) {
            "这是一个假的 BRAND"
        } else {
            Build.BRAND
        }
    }

}