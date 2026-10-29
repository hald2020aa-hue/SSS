package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class SssVisualConfig(
    val neonTheme: SssNeonTheme = SssNeonTheme.CYBER_CYAN,
    val layoutMode: String = "GRID_3",
    val iconStyle: String = "NEON_GLOW",
    val navigationBarStyle: String = "FLOATING_PILL",
    val homeSectionOrder: String = "FEATURED_RECENT_POPULAR",
    val cardCornerRadiusDp: Int = 16,
    val neonGlowIntensity: Int = 85,
    val readerMode: String = "WEBTOON_VERTICAL"
)

val LocalSssVisualConfig = staticCompositionLocalOf { SssVisualConfig() }

@Composable
fun SssTheme(
    visualConfig: SssVisualConfig = SssVisualConfig(),
    content: @Composable () -> Unit
) {
    val palette = visualConfig.neonTheme
    val colorScheme = darkColorScheme(
        primary = palette.primaryNeon,
        onPrimary = Color(0xFF04060C),
        primaryContainer = palette.surfaceElevated,
        onPrimaryContainer = palette.primaryNeon,
        secondary = palette.secondaryNeon,
        onSecondary = Color(0xFF04060C),
        secondaryContainer = palette.surfaceElevated,
        onSecondaryContainer = palette.secondaryNeon,
        tertiary = palette.tertiaryNeon,
        background = palette.background,
        onBackground = palette.onBackground,
        surface = palette.surface,
        onSurface = palette.onBackground,
        surfaceVariant = palette.surfaceElevated,
        onSurfaceVariant = palette.onSurfaceMuted,
        outline = palette.primaryNeon.copy(alpha = 0.45f)
    )

    val radius = visualConfig.cardCornerRadiusDp.coerceIn(4, 32).dp
    val shapes = Shapes(
        small = RoundedCornerShape((radius.value * 0.6f).dp),
        medium = RoundedCornerShape(radius),
        large = RoundedCornerShape((radius.value * 1.25f).dp)
    )

    CompositionLocalProvider(LocalSssVisualConfig provides visualConfig) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = shapes,
            content = content
        )
    }
}
