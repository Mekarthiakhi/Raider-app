package com.fleettrack.app.ui.owner.fleet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.model.RideStatus
import com.fleettrack.app.data.model.VehicleStatus
import com.fleettrack.app.ui.components.EarningsText
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.components.StatItem
import com.fleettrack.app.ui.components.StatusDot
import com.fleettrack.app.ui.components.VehicleStatusState
import com.fleettrack.app.ui.theme.BackgroundDark
import com.fleettrack.app.ui.theme.EmeraldAccent
import com.fleettrack.app.ui.theme.EarningsGold
import com.fleettrack.app.ui.theme.SurfaceBorderDark
import com.fleettrack.app.ui.theme.TextMuted
import com.fleettrack.app.ui.theme.TextPrimary
import com.fleettrack.app.ui.theme.TextSecondary
import com.fleettrack.app.ui.theme.TileShape
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FleetBoardScreen(
    viewModel: FleetBoardViewModel,
    onVehicleClick: (Long) -> Unit,
    onReviewQueueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val overview by viewModel.overview.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Fleet Operations",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "30 Active Vehicles",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            // Review Queue quick link
            GlassCard(
                onClick = onReviewQueueClick,
                shape = TileShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Review Queue",
                        color = EmeraldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero Earnings Card (Span full row)
            item(span = { GridItemSpan(2) }) {
                HeroEarningsCard(
                    totalEarnings = overview.totalEarningsToday,
                    totalRides = overview.totalRidesToday,
                    totalKm = overview.totalKmToday,
                    activeCars = overview.activeVehicles
                )
            }

            // Grid of 30 Vehicle Tiles
            items(vehicles, key = { it.vehicleId }) { vehicle ->
                VehicleTile(
                    vehicle = vehicle,
                    onClick = { onVehicleClick(vehicle.vehicleId) }
                )
            }
        }
    }
}

@Composable
private fun HeroEarningsCard(
    totalEarnings: Double,
    totalRides: Int,
    totalKm: Double,
    activeCars: Int
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S FLEET REVENUE",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(state = VehicleStatusState.ON_TRIP, size = 6.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE",
                        color = EmeraldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            EarningsText(amount = totalEarnings)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Rides", value = "$totalRides")
                StatItem(label = "Distance", value = "${String.format(Locale.US, "%.1f", totalKm)} km")
                StatItem(
                    label = "Active Fleet",
                    value = "$activeCars / 30",
                    valueColor = EmeraldAccent
                )
            }
        }
    }
}

@Composable
private fun VehicleTile(
    vehicle: VehicleStatus,
    onClick: () -> Unit
) {
    val statusState = when {
        !vehicle.isOnline -> VehicleStatusState.OFFLINE
        vehicle.currentRideStatus == RideStatus.IN_TRIP -> VehicleStatusState.ON_TRIP
        else -> VehicleStatusState.IDLE
    }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    GlassCard(
        onClick = onClick,
        shape = TileShape,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vehicle.regNo,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                StatusDot(state = statusState, size = 8.dp)
            }

            if (!vehicle.nickname.isNullOrBlank()) {
                Text(
                    text = vehicle.nickname,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = currencyFormat.format(vehicle.todayFare),
                color = EarningsGold,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${vehicle.todayRides} rides",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", vehicle.todayKm)} km",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

