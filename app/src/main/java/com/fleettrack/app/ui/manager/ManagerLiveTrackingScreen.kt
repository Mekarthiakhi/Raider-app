package com.fleettrack.app.ui.manager

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fleettrack.app.ui.owner.tracking.LiveTrackingScreen

@Composable
fun ManagerLiveTrackingScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiveTrackingScreen(
        onBackClick = onNavigateBack,
        modifier = modifier.fillMaxSize()
    )
}

