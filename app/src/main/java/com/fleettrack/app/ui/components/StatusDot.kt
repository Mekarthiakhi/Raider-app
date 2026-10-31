package com.fleettrack.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fleettrack.app.ui.theme.AlertRed
import com.fleettrack.app.ui.theme.EmeraldAccent
import com.fleettrack.app.ui.theme.StatusIdle

enum class VehicleStatusState {
    ON_TRIP,
    IDLE,
    OFFLINE
}

@Composable
fun StatusDot(
    state: VehicleStatusState,
    modifier: Modifier = Modifier,
    size: Dp = 8.dp
) {
    val color = when (state) {
        VehicleStatusState.ON_TRIP -> EmeraldAccent
        VehicleStatusState.IDLE -> StatusIdle
        VehicleStatusState.OFFLINE -> AlertRed
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

