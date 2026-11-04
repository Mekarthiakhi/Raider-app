package com.fleettrack.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val FleetLightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = SurfaceWhite,
    primaryContainer = PrimaryPurpleContainer,
    onPrimaryContainer = PrimaryPurpleDark,

    secondary = ActiveGreen,
    onSecondary = SurfaceWhite,
    secondaryContainer = ActiveGreenBg,
    onSecondaryContainer = ActiveGreen,

    error = InactiveRed,
    onError = SurfaceWhite,
    errorContainer = InactiveRedBg,
    onErrorContainer = InactiveRed,

    background = BackgroundLight,
    onBackground = TextDark,

    surface = SurfaceWhite,
    onSurface = TextDark,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextGray,

    outline = SurfaceBorder
)

@Composable
fun FleetTrackTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FleetLightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
