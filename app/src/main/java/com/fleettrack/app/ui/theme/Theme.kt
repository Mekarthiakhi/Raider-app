package com.fleettrack.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun buildLightColorScheme() = lightColorScheme(
    primary = Color(0xFF5B36F5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEEF0FF),
    onPrimaryContainer = Color(0xFF4A28DF),

    secondary = Color(0xFF10B981),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE6F9F2),
    onSecondaryContainer = Color(0xFF10B981),

    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEECEC),
    onErrorContainer = Color(0xFFEF4444),

    background = Color(0xFFF7F8FC),
    onBackground = Color(0xFF16181F),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF16181F),
    surfaceVariant = Color(0xFFF7F8FC),
    onSurfaceVariant = Color(0xFF6B7280),
    surfaceContainerHigh = Color(0xFFFFFFFF),

    outline = Color(0xFFECEEF6)
)

private fun buildDarkColorScheme() = darkColorScheme(
    primary = Color(0xFF7C66FF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF231F4B),
    onPrimaryContainer = Color(0xFFE0DCFF),

    secondary = Color(0xFF10B981),
    onSecondary = Color(0xFF04261B),
    secondaryContainer = Color(0xFF0E3429),
    onSecondaryContainer = Color(0xFF6EE7B7),

    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF3B161E),
    onErrorContainer = Color(0xFFFCA5A5),

    background = Color(0xFF080D1B),
    onBackground = Color(0xFFF8FAFC),

    surface = Color(0xFF11182A),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF161F33),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainerHigh = Color(0xFF161F33),

    outline = Color(0xFF263149)
)

@Composable
fun FleetTrackTheme(
    darkTheme: Boolean = ThemeManager.isDarkTheme,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) buildDarkColorScheme() else buildLightColorScheme(),
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
