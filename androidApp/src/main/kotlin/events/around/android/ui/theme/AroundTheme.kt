package events.around.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import events.around.core.model.ThemeMode

private val Accent = Color(0xFFFF5A36)

// White and neutral greys as in the designs; no dynamic colour, which would tint every surface.
private val LightColors = lightColorScheme(
    primary = Accent,
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFEBEBEB),
    onSurface = Color(0xFF141414),
    onSurfaceVariant = Color(0xFF8C8C8C),
    outlineVariant = Color(0xFFE9E9E9),
)
private val DarkColors = darkColorScheme(
    primary = Accent,
    background = Color(0xFF111111),
    surface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFF3A3A3C),
    onSurface = Color(0xFFF2F2F2),
    onSurfaceVariant = Color(0xFF9A9A9A),
    outlineVariant = Color(0xFF333333),
)

/** Whether the app draws dark: the palette below picks its greys by it. */
val LocalDarkTheme = staticCompositionLocalOf { false }

@Composable
fun AroundTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    CompositionLocalProvider(LocalDarkTheme provides dark) {
        MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
    }
}
