package com.fleettrack.app.ui.driver.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.ui.components.EarningsText
import com.fleettrack.app.ui.components.EmptyState
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.components.PlatformChip
import com.fleettrack.app.ui.components.StatItem
import com.fleettrack.app.ui.components.StatusDot
import com.fleettrack.app.ui.components.VehicleStatusState
import com.fleettrack.app.ui.theme.AlertRed
import com.fleettrack.app.ui.theme.BackgroundDark
import com.fleettrack.app.ui.theme.ChipShape
import com.fleettrack.app.ui.theme.EmeraldAccent
import com.fleettrack.app.ui.theme.EmeraldAccentContainer
import com.fleettrack.app.ui.theme.EarningsGold
import com.fleettrack.app.ui.theme.TextMuted
import com.fleettrack.app.ui.theme.TextPrimary
import com.fleettrack.app.ui.theme.TextSecondary
import com.fleettrack.app.ui.theme.TileShape
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DriverTodayScreen(
    viewModel: DriverTodayViewModel,
    modifier: Modifier = Modifier
) {
    val isTrackingActive by viewModel.isTrackingActive.collectAsState()
    val summary by viewModel.todaySummary.collectAsState()
    val rides by viewModel.todayRides.collectAsState()

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
                    text = "Driver Companion",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Shift Telemetry",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            // Tracking Active Status Pill
            Box(
                modifier = Modifier
                    .background(
                        color = if (isTrackingActive) EmeraldAccentContainer else AlertRed.copy(alpha = 0.2f),
                        shape = ChipShape
                    )
                    .border(
                        width = 1.dp,
                        color = if (isTrackingActive) EmeraldAccent else AlertRed,
                        shape = ChipShape
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(
                        state = if (isTrackingActive) VehicleStatusState.ON_TRIP else VehicleStatusState.OFFLINE,
                        size = 6.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTrackingActive) "Tracking Active" else "Tracking Paused",
                        color = if (isTrackingActive) EmeraldAccent else AlertRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Driver Earnings Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "TODAY'S RIDE FARES",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                EarningsText(amount = summary.totalFare)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(label = "Trips", value = "${summary.rideCount}")
                    StatItem(
                        label = "Distance",
                        value = "${String.format(Locale.US, "%.1f", summary.totalKm)} km"
                    )
                    StatItem(
                        label = "Avg Fare / Km",
                        value = "₹${String.format(Locale.US, "%.1f", summary.avgFarePerKm)}",
                        valueColor = EmeraldAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Completed Trips",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (rides.isEmpty()) {
            EmptyState(
                title = "No trips logged yet today",
                subtitle = "Accept and complete rides in Rapido, Uber, or Ola. The companion will auto-detect each trip."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(rides, key = { it.id }) { ride ->
                    DriverRideItem(ride = ride)
                }
            }
        }
    }
}

@Composable
private fun DriverRideItem(ride: RideEntity) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlatformChip(platform = ride.platform)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${ride.distanceKm ?: 0.0} km • ${ride.durationMin?.toInt() ?: 0} min",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = ride.status.name,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = ride.fare?.let { currencyFormat.format(it) } ?: "--",
                color = EarningsGold,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

