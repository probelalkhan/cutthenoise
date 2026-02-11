package dev.belalkhan.cutthenoise.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ClarityDarkScheme = darkColorScheme(
    primary = ElectricTeal,
    onPrimary = TextOnAccent,
    primaryContainer = ElectricTealDim,
    onPrimaryContainer = TextPrimary,
    secondary = SlateBlueStoic,
    onSecondary = TextOnAccent,
    tertiary = SunsetOrangeOptimist,
    onTertiary = TextOnAccent,
    background = NightBlack,
    onBackground = TextPrimary,
    surface = DarkCharcoal,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = TextOnAccent,
    outline = DividerDark,
)

@Composable
fun CutTheNoiseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ClarityDarkScheme,
        typography = Typography,
        content = content
    )
}