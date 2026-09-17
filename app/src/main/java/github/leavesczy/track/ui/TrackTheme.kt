package github.leavesczy.track.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import github.leavesczy.track.R

@Composable
internal fun TrackTheme(content: @Composable () -> Unit) {
    val lightColorScheme = remember {
        lightColorScheme(
            primary = Color(color = 0xFF1E88E5),
            onPrimary = Color.White,
            primaryContainer = Color(color = 0xFFE3F2FD),
            onPrimaryContainer = Color(color = 0xFF1565C0),
            secondary = Color(color = 0xFF1E88E5),
            surface = Color(color = 0xFFF5F7FA),
            onSurface = Color(color = 0xFF1A2332),
            onSurfaceVariant = Color(color = 0xFF5C6B7A)
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

@Composable
internal fun TrackTopAppBar(title: String) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(id = R.color.color_primary),
            titleContentColor = colorResource(id = R.color.color_on_primary)
        ),
        title = {
            Text(
                text = title,
                fontSize = 20.sp
            )
        }
    )
}
