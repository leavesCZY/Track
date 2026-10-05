package github.leavesczy.track.member

import android.os.Build

internal object SystemFieldProxy {

    @JvmField
    var BRAND = "这是一个假的 BRAND"

    fun onProxyEnabledChanged() {
        BRAND = if (MemberTrackActivity.isProxyEnabled) {
            "这是一个假的 BRAND"
        } else {
            Build.BRAND
        }
    }

}
