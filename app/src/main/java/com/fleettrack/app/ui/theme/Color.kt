package com.fleettrack.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// Fleet Manager Purple / Adaptive Light & Dark Design System
// ==========================================

// Brand Accents (Readable in both light and dark surfaces)
val PrimaryPurple: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF7C66FF) else Color(0xFF5B36F5)
val PrimaryPurpleDark: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF6655F5) else Color(0xFF4A28DF)
val PrimaryPurpleLight = Color(0xFF7A5CFF)
val PrimaryPurpleContainer: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF231F4B) else Color(0xFFEEF0FF)

// Hero Gradient for Today's Earnings Card
val PurpleGradient = Brush.horizontalGradient(
    listOf(Color(0xFF6C47FF), Color(0xFF4E2CE8))
)

val PurpleChartGradient = Brush.verticalGradient(
    listOf(Color(0xFF6C47FF).copy(alpha = 0.35f), Color(0xFF6C47FF).copy(alpha = 0.0f))
)

// Adaptive Background & Surface Hierarchy
val BackgroundLight: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF080D1B) else Color(0xFFF7F8FC)
val SurfaceWhite: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF11182A) else Color(0xFFFFFFFF)
val SurfaceCard: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF161F33) else Color(0xFFFFFFFF)
val SurfaceBorder: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF263149) else Color(0xFFECEEF6)
val DividerLight: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF1E273D) else Color(0xFFF1F3F9)

// Metric Pill Container & Text Accents
val MetricPurple: Color
    get() = PrimaryPurple
val MetricPurpleBg: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF211E47) else Color(0xFFEFF0FF)

val ActiveGreen = Color(0xFF10B981)
val ActiveGreenBg: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF0E3429) else Color(0xFFE6F9F2)

val InactiveRed = Color(0xFFEF4444)
val InactiveRedBg: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF3B161E) else Color(0xFFFEECEC)

val IdleOrange = Color(0xFFF59E0B)
val IdleOrangeBg: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF3A280E) else Color(0xFFFEF3C7)

// Adaptive Typography & Content
val TextDark: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF16181F)
val TextGray: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF94A3B8) else Color(0xFF6B7280)
val TextMutedGray: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF64748B) else Color(0xFF9CA3AF)

// Platform Specific Badges
val UberBlack = Color(0xFF000000)
val UberWhiteText = Color(0xFFFFFFFF)

val RapidoYellowBg: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF3B2F0E) else Color(0xFFFFF7D6)
val RapidoYellowText: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFFFBBF24) else Color(0xFFB45309)

val OlaGreenBg: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF11311E) else Color(0xFFE8F5E9)
val OlaGreenText: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF4ADE80) else Color(0xFF2E7D32)

// Backward Compatibility Aliases (dynamically adapt with active theme)
val BackgroundDark: Color
    get() = BackgroundLight
val SurfaceDark: Color
    get() = SurfaceWhite
val SurfaceElevatedDark: Color
    get() = SurfaceCard
val SurfaceBorderDark: Color
    get() = SurfaceBorder
val CardBackgroundGlass: Color
    get() = SurfaceWhite
val TextPrimary: Color
    get() = TextDark
val TextSecondary: Color
    get() = TextGray
val TextMuted: Color
    get() = TextMutedGray
val DividerColor: Color
    get() = DividerLight
val EmeraldAccent = ActiveGreen
val EmeraldAccentContainer: Color
    get() = ActiveGreenBg
val EmeraldAccentLight = ActiveGreen
val EarningsGold: Color
    get() = PrimaryPurple
val EarningsGoldLight = PrimaryPurpleLight
val EarningsGoldContainer: Color
    get() = PrimaryPurpleContainer
val AlertRed = InactiveRed
val AlertRedContainer: Color
    get() = InactiveRedBg
val StatusIdle = IdleOrange
val UberDark = UberBlack
val UberWhite = UberWhiteText
val RapidoYellow = Color(0xFFF5C518)
val RapidoYellowContainer: Color
    get() = RapidoYellowBg
val OlaLime = Color(0xFF87C830)
val OlaLimeContainer: Color
    get() = OlaGreenBg
val InDriveGreen = Color(0xFF26CD58)
val InDriveGreenContainer: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF0E3422) else Color(0xFFE6F9EE)
val NammaYatriBlue = Color(0xFF1E88E5)
val NammaYatriBlueContainer: Color
    get() = if (ThemeManager.isDarkTheme) Color(0xFF122846) else Color(0xFFE3F2FD)
