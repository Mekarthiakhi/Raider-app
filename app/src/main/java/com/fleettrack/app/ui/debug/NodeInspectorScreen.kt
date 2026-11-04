package com.fleettrack.app.ui.debug

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.service.statemachine.TelemetryLogEntry
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.components.PlatformChip
import com.fleettrack.app.ui.components.StatusDot
import com.fleettrack.app.ui.components.VehicleStatusState
import com.fleettrack.app.ui.theme.AlertRed
import com.fleettrack.app.ui.theme.BackgroundDark
import com.fleettrack.app.ui.theme.EmeraldAccent
import com.fleettrack.app.ui.theme.EarningsGold
import com.fleettrack.app.ui.theme.SurfaceBorderDark
import com.fleettrack.app.ui.theme.SurfaceDark
import com.fleettrack.app.ui.theme.TextMuted
import com.fleettrack.app.ui.theme.TextPrimary
import com.fleettrack.app.ui.theme.TextSecondary
import com.fleettrack.app.ui.theme.TileShape

@Composable
fun NodeInspectorScreen(
    viewModel: NodeInspectorViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTrip by viewModel.activeTrip.collectAsState()
    val telemetryLogs by viewModel.telemetryLogs.collectAsState()
    val dumpText by viewModel.currentDumpText.collectAsState()
    val isA11yEnabled by viewModel.isServiceEnabled.collectAsState()
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Node Hierarchy", "Telemetry Stream", "Ride Simulator")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Debug Node Inspector",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Capture real driver app view IDs",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = { viewModel.refreshDump() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = EmeraldAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Accessibility Service Status Banner
        GlassCard(
            shape = TileShape,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(
                        state = if (isA11yEnabled) VehicleStatusState.ON_TRIP else VehicleStatusState.OFFLINE,
                        size = 8.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isA11yEnabled) "Accessibility Service: ACTIVE" else "Accessibility: DISABLED",
                        color = if (isA11yEnabled) EmeraldAccent else AlertRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "State: ${activeTrip?.status?.name ?: "IDLE"}",
                    color = if (activeTrip != null) EarningsGold else TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = SurfaceDark,
            contentColor = EmeraldAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = EmeraldAccent
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTabIndex) {
            0 -> NodeHierarchyTab(
                dumpText = dumpText,
                onCopy = {
                    if (viewModel.copyDumpToClipboard()) {
                        Toast.makeText(context, "Node dump copied! Paste into chat.", Toast.LENGTH_LONG).show()
                    }
                }
            )

            1 -> TelemetryStreamTab(logs = telemetryLogs)

            2 -> SimulatorTab(
                onSimulateRapido = { viewModel.simulateRide(Platform.RAPIDO, 215.0, 7.4) },
                onSimulateUber = { viewModel.simulateRide(Platform.UBER, 380.0, 14.2) },
                onSimulateOla = { viewModel.simulateRide(Platform.OLA, 165.0, 5.1) },
                onTriggerSync = {
                    viewModel.triggerSync()
                    Toast.makeText(context, "Outbox sync triggered", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun NodeHierarchyTab(
    dumpText: String,
    onCopy: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Screen View Tree",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Button(
                onClick = onCopy,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = BackgroundDark,
                    modifier = Modifier.height(16.dp).width(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Copy Dump", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = dumpText,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryStreamTab(logs: List<TelemetryLogEntry>) {
    if (logs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No telemetry events yet.\nSwitch to Rapido, Uber, or Ola to log window changes.",
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs) { log ->
                GlassCard(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                log.platform?.let {
                                    PlatformChip(platform = it)
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = log.eventType,
                                    color = EmeraldAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            log.fare?.let {
                                Text(
                                    text = "₹${it.toInt()}",
                                    color = EarningsGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = log.snippet,
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulatorTab(
    onSimulateRapido: () -> Unit,
    onSimulateUber: () -> Unit,
    onSimulateOla: () -> Unit,
    onTriggerSync: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Test Telemetry Pipeline",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Simulate complete trips to verify State Machine transitions, Room database persistence, and Supabase cloud sync.",
            color = TextMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onSimulateRapido,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = EmeraldAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simulate Rapido Ride (₹215, 7.4 km)")
        }

        OutlinedButton(
            onClick = onSimulateUber,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = EmeraldAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simulate Uber Ride (₹380, 14.2 km)")
        }

        OutlinedButton(
            onClick = onSimulateOla,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = EmeraldAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simulate Ola Ride (₹165, 5.1 km)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onTriggerSync,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
        ) {
            Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = BackgroundDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Trigger Immediate Cloud Outbox Sync", color = BackgroundDark, fontWeight = FontWeight.Bold)
        }
    }
}

