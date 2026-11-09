package events.around.android.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/** The design's greys and accents (Figma Luma Gray and Luma Color styles), with dark-mode counterparts. */
object AroundColors {
    val ink: Color
        @Composable @ReadOnlyComposable
        get() = pick(0xFF141414, 0xFFF2F2F2)
    val secondary: Color
        @Composable @ReadOnlyComposable
        get() = pick(0xFF8C8C8C, 0xFF9A9A9A)
    val tertiary: Color
        @Composable @ReadOnlyComposable
        get() = pick(0xFFA6A6A6, 0xFF7A7A7A)
    val weekday: Color
        @Composable @ReadOnlyComposable
        get() = pick(0xFF9A9A9A, 0xFF8A8A8A)
    val slash: Color
        @Composable @ReadOnlyComposable
        get() = pick(0xFFD4D4D4, 0xFF4A4A4A)
    val chevron: Color
        @Composable @ReadOnlyComposable
        get() = pick(0xFFBDBDBD, 0xFF5A5A5A)
    val separator: Color
        @Composable @ReadOnlyComposable
        get() = pick(0xFFE2E2E2, 0xFF2E2E2E)
    val chipBorder: Color
        @Composable @ReadOnlyComposable
        get() = pick(0xFFE9E9E9, 0xFF333333)
    val chipText: Color
        @Composable @ReadOnlyComposable
        get() = pick(0xFF6B6B6B, 0xFFB0B0B0)
    val chipCount: Color
        @Composable @ReadOnlyComposable
        get() = pick(0xFFABABAB, 0xFF6E6E6E)
    val price: Color
        @Composable @ReadOnlyComposable
        get() = pick(0xFF3DA92B, 0xFF5CC94A)
    val priceBackground: Color
        @Composable @ReadOnlyComposable
        get() = pick(0xFFF4FAF2, 0xFF1C2A1A)
    val surface: Color
        @Composable @ReadOnlyComposable
        get() = pick(0xFFFFFFFF, 0xFF1C1C1E)
    val tabSelected: Color
        @Composable @ReadOnlyComposable
        get() = pick(0xFFEBEBEB, 0xFF3A3A3C)
    val tabInactive: Color
        @Composable @ReadOnlyComposable
        get() = pick(0xFFA8A8A8, 0xFF8E8E93)

    @Composable
    @ReadOnlyComposable
    private fun pick(light: Long, dark: Long): Color = Color(if (LocalDarkTheme.current) dark else light)
}
