package com.fleettrack.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// Fleet Manager Purple / Clean White Design System
// ==========================================

// Primary Brand Colors
val PrimaryPurple = Color(0xFF5B36F5)
val PrimaryPurpleDark = Color(0xFF4A28DF)
val PrimaryPurpleLight = Color(0xFF7A5CFF)
val PrimaryPurpleContainer = Color(0xFFEEF0FF)

// Hero Gradient for Today's Earnings Card
val PurpleGradient = Brush.horizontalGradient(
    listOf(Color(0xFF6C47FF), Color(0xFF4E2CE8))
)

val PurpleChartGradient = Brush.verticalGradient(
    listOf(Color(0xFF6C47FF).copy(alpha = 0.35f), Color(0xFF6C47FF).copy(alpha = 0.0f))
)

// Clean Light Background & Surface Hierarchy
val BackgroundLight = Color(0xFFF7F8FC)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceCard = Color(0xFFFFFFFF)
val SurfaceBorder = Color(0xFFECEEF6)
val DividerLight = Color(0xFFF1F3F9)

// Metric Pill Container & Text Accents
val MetricPurple = Color(0xFF5B36F5)
val MetricPurpleBg = Color(0xFFEFF0FF)

val ActiveGreen = Color(0xFF10B981)
val ActiveGreenBg = Color(0xFFE6F9F2)

val InactiveRed = Color(0xFFEF4444)
val InactiveRedBg = Color(0xFFFEECEC)

val IdleOrange = Color(0xFFF59E0B)
val IdleOrangeBg = Color(0xFFFEF3C7)

// Typography & Content
val TextDark = Color(0xFF16181F)
val TextGray = Color(0xFF6B7280)
val TextMutedGray = Color(0xFF9CA3AF)

// Platform Specific Badges
val UberBlack = Color(0xFF000000)
val UberWhiteText = Color(0xFFFFFFFF)

val RapidoYellowBg = Color(0xFFFFF7D6)
val RapidoYellowText = Color(0xFFB45309)

val OlaGreenBg = Color(0xFFE8F5E9)
val OlaGreenText = Color(0xFF2E7D32)

// Backward Compatibility Aliases (for existing components)
val BackgroundDark = BackgroundLight
val SurfaceDark = SurfaceWhite
val SurfaceElevatedDark = SurfaceWhite
val SurfaceBorderDark = SurfaceBorder
val CardBackgroundGlass = SurfaceWhite
val TextPrimary = TextDark
val TextSecondary = TextGray
val TextMuted = TextMutedGray
val DividerColor = DividerLight
val EmeraldAccent = ActiveGreen
val EmeraldAccentContainer = ActiveGreenBg
val EmeraldAccentLight = ActiveGreen
val EarningsGold = PrimaryPurple
val EarningsGoldLight = PrimaryPurpleLight
val EarningsGoldContainer = PrimaryPurpleContainer
val AlertRed = InactiveRed
val AlertRedContainer = InactiveRedBg
val StatusIdle = IdleOrange
val UberDark = UberBlack
val UberWhite = UberWhiteText
val RapidoYellow = Color(0xFFF5C518)
val RapidoYellowContainer = RapidoYellowBg
val OlaLime = Color(0xFF87C830)
val OlaLimeContainer = OlaGreenBg
val InDriveGreen = Color(0xFF26CD58)
val InDriveGreenContainer = Color(0xFFE6F9EE)
val NammaYatriBlue = Color(0xFF1E88E5)
val NammaYatriBlueContainer = Color(0xFFE3F2FD)
