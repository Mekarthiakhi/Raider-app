package com.fleettrack.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.model.Confidence
import com.fleettrack.app.ui.theme.AlertRed
import com.fleettrack.app.ui.theme.AlertRedContainer
import com.fleettrack.app.ui.theme.ChipShape
import com.fleettrack.app.ui.theme.EmeraldAccent
import com.fleettrack.app.ui.theme.EmeraldAccentContainer
import com.fleettrack.app.ui.theme.EarningsGold
import com.fleettrack.app.ui.theme.EarningsGoldContainer

@Composable
fun ConfidenceBadge(
    confidence: Confidence,
    modifier: Modifier = Modifier
) {
    val (bg, textColor, borderColor) = when (confidence.level) {
        Confidence.Level.HIGH -> Triple(
            EmeraldAccentContainer,
            EmeraldAccent,
            EmeraldAccent.copy(alpha = 0.4f)
        )
        Confidence.Level.MEDIUM -> Triple(
            EarningsGoldContainer,
            EarningsGold,
            EarningsGold.copy(alpha = 0.4f)
        )
        Confidence.Level.LOW -> Triple(
            AlertRedContainer,
            AlertRed,
            AlertRed.copy(alpha = 0.4f)
        )
    }

    Box(
        modifier = modifier
            .background(color = bg, shape = ChipShape)
            .border(width = 1.dp, color = borderColor, shape = ChipShape)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = "${confidence.level.label} (${(confidence.score * 100).toInt()}%)",
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

