package com.fleettrack.app.ui.owner.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.local.entity.AlertEntity
import com.fleettrack.app.ui.components.EmptyState
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.theme.AlertRed
import com.fleettrack.app.ui.theme.BackgroundDark
import com.fleettrack.app.ui.theme.EmeraldAccent
import com.fleettrack.app.ui.theme.TextMuted
import com.fleettrack.app.ui.theme.TextPrimary
import com.fleettrack.app.ui.theme.TextSecondary
import com.fleettrack.app.ui.theme.TileShape

@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel,
    modifier: Modifier = Modifier
) {
    val alerts by viewModel.unreadAlerts.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Fleet Alerts",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Inactivity, accessibility and battery warnings",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            if (alerts.isNotEmpty()) {
                TextButton(onClick = { viewModel.markAllRead() }) {
                    Text("Clear All", color = EmeraldAccent)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (alerts.isEmpty()) {
            EmptyState(
                title = "All systems operational",
                subtitle = "No offline cars or accessibility service interruptions detected."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(alerts, key = { it.id }) { alert ->
                    AlertItem(
                        alert = alert,
                        onDismiss = { viewModel.markRead(alert.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertItem(
    alert: AlertEntity,
    onDismiss: () -> Unit
) {
    GlassCard(
        shape = TileShape,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AlertRed
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = alert.alertType.replace('_', ' ').uppercase(),
                        color = AlertRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = alert.message,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Dismiss",
                    tint = TextSecondary
                )
            }
        }
    }
}

