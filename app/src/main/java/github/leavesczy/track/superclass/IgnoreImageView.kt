package github.leavesczy.track.superclass

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

/** 命中 superclassTrack 的 exclude，直接父类保持 ImageView。 */
@SuppressLint("AppCompatCustomView")
class IgnoreImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ImageView(context, attrs, defStyleAttr, defStyleRes)
