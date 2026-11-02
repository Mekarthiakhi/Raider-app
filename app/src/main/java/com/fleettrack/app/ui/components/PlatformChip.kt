package com.fleettrack.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.ui.theme.ChipShape
import com.fleettrack.app.ui.theme.InDriveGreen
import com.fleettrack.app.ui.theme.InDriveGreenContainer
import com.fleettrack.app.ui.theme.NammaYatriBlue
import com.fleettrack.app.ui.theme.NammaYatriBlueContainer
import com.fleettrack.app.ui.theme.OlaLime
import com.fleettrack.app.ui.theme.OlaLimeContainer
import com.fleettrack.app.ui.theme.RapidoYellow
import com.fleettrack.app.ui.theme.RapidoYellowContainer
import com.fleettrack.app.ui.theme.UberDark
import com.fleettrack.app.ui.theme.UberWhite

@Composable
fun PlatformChip(
    platform: Platform,
    modifier: Modifier = Modifier
) {
    val (bg, textColor, borderColor) = when (platform) {
        Platform.RAPIDO -> Triple(RapidoYellowContainer, RapidoYellow, RapidoYellow.copy(alpha = 0.4f))
        Platform.UBER -> Triple(UberDark, UberWhite, UberWhite.copy(alpha = 0.3f))
        Platform.OLA -> Triple(OlaLimeContainer, OlaLime, OlaLime.copy(alpha = 0.4f))
        Platform.INDRIVE -> Triple(InDriveGreenContainer, InDriveGreen, InDriveGreen.copy(alpha = 0.4f))
        Platform.NAMMA_YATRI -> Triple(NammaYatriBlueContainer, NammaYatriBlue, NammaYatriBlue.copy(alpha = 0.4f))
    }

    Box(
        modifier = modifier
            .background(color = bg, shape = ChipShape)
            .border(width = 1.dp, color = borderColor, shape = ChipShape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = platform.displayName,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
