package github.leavesczy.track.member

import android.os.Build

/** GETSTATIC 替换只换 owner，proxy 需提供同名同类型字段。 */
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
