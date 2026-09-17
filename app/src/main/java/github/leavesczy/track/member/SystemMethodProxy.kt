package github.leavesczy.track.member

import android.content.ContentResolver

internal object SystemMethodProxy {

    @JvmStatic
    fun getString(resolver: ContentResolver, name: String): String {
        return "proxy-android-id"
    }

}
