package github.leavesczy.track.superclass

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

/** 源码继承 ImageView，插桩后直接父类改为 [MonitorImageView]。 */
@SuppressLint("AppCompatCustomView")
class CustomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ImageView(context, attrs, defStyleAttr, defStyleRes)
