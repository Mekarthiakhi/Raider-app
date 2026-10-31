package com.fleettrack.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldAccent,
    onPrimary = BackgroundDark,
    primaryContainer = EmeraldAccentContainer,
    onPrimaryContainer = EmeraldAccentLight,

    secondary = EarningsGold,
    onSecondary = BackgroundDark,
    secondaryContainer = EarningsGoldContainer,
    onSecondaryContainer = EarningsGoldLight,

    error = AlertRed,
    onError = BackgroundDark,
    errorContainer = AlertRedContainer,
    onErrorContainer = AlertRed,

    background = BackgroundDark,
    onBackground = TextPrimary,

    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevatedDark,
    onSurfaceVariant = TextSecondary,

    outline = SurfaceBorderDark
)

@Composable
fun FleetTrackTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

