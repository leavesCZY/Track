package github.leavesczy.track.click.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * @Author: leavesCZY
 * @Date: 2025/5/16 11:43
 * @Desc:
 */
@Composable
internal fun TrackTheme(content: @Composable () -> Unit) {
    val lightColorScheme = remember {
        lightColorScheme(
            primary = Color(color = 0xFF03A9F4),
            secondary = Color(color = 0xFF03A9F4),
            tertiary = Color(color = 0xFF7D5260)
        )
    }
    val typography = remember {
        Typography(
            bodyLarge = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            )
        )
    }
    MaterialTheme(
        colorScheme = lightColorScheme,
        typography = typography,
        content = content
    )
}