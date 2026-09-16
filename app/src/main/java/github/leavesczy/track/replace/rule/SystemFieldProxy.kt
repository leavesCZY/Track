package github.leavesczy.track.replace.rule

import android.os.Build

internal object SystemFieldProxy {

    @JvmField
    var BRAND = "这是一个假的 BRAND"

    fun onProxyEnabledChanged() {
        BRAND = if (ReplaceRuleTrackActivity.isProxyEnabled) {
            "这是一个假的 BRAND"
        } else {
            Build.BRAND
        }
    }

}
