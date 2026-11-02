package com.fleettrack.app.ui.owner.fleet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.model.DriverPerformance
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import com.fleettrack.app.data.model.VehicleStatus
import com.fleettrack.app.ui.components.EarningsText
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
import com.fleettrack.app.ui.theme.EarningsGoldLight
import com.fleettrack.app.ui.theme.SurfaceBorderDark
import com.fleettrack.app.ui.theme.SurfaceDark
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
    val drivers by viewModel.drivers.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen Top Bar
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
                    text = "30 Cabs • Live Uber & Rapido",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            // Review Queue Link
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

        Spacer(modifier = Modifier.height(14.dp))

        // View Mode Switcher Tabs (30 Cabs vs Employees)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, shape = TileShape)
                .border(1.dp, SurfaceBorderDark, TileShape)
                .padding(4.dp)
        ) {
            // Cabs Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (selectedTab == FleetBoardTab.VEHICLES) EmeraldAccentContainer else SurfaceDark,
                        shape = TileShape
                    )
                    .clickable { viewModel.selectTab(FleetBoardTab.VEHICLES) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = if (selectedTab == FleetBoardTab.VEHICLES) EmeraldAccent else TextMuted,
                        modifier = Modifier.height(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "30 Cabs (${vehicles.size})",
                        color = if (selectedTab == FleetBoardTab.VEHICLES) EmeraldAccent else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Employees Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (selectedTab == FleetBoardTab.DRIVERS) EmeraldAccentContainer else SurfaceDark,
                        shape = TileShape
                    )
                    .clickable { viewModel.selectTab(FleetBoardTab.DRIVERS) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = if (selectedTab == FleetBoardTab.DRIVERS) EmeraldAccent else TextMuted,
                        modifier = Modifier.height(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Employees (${drivers.size})",
                        color = if (selectedTab == FleetBoardTab.DRIVERS) EmeraldAccent else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Content based on Tab
        when (selectedTab) {
            FleetBoardTab.VEHICLES -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Hero Revenue Card (Span full 2 columns)
                    item(span = { GridItemSpan(2) }) {
                        HeroEarningsCard(
                            totalEarnings = overview.totalEarningsToday,
                            totalRides = overview.totalRidesToday,
                            totalKm = overview.totalKmToday,
                            activeCars = overview.activeVehicles,
                            uberEarnings = overview.uberEarnings,
                            uberRides = overview.uberRides,
                            rapidoEarnings = overview.rapidoEarnings,
                            rapidoRides = overview.rapidoRides
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

            FleetBoardTab.DRIVERS -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        HeroEarningsCard(
                            totalEarnings = overview.totalEarningsToday,
                            totalRides = overview.totalRidesToday,
                            totalKm = overview.totalKmToday,
                            activeCars = overview.activeVehicles,
                            uberEarnings = overview.uberEarnings,
                            uberRides = overview.uberRides,
                            rapidoEarnings = overview.rapidoEarnings,
                            rapidoRides = overview.rapidoRides
                        )
                    }

                    items(drivers, key = { it.driverId }) { driver ->
                        DriverPerformanceTile(driver = driver)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroEarningsCard(
    totalEarnings: Double,
    totalRides: Int,
    totalKm: Double,
    activeCars: Int,
    uberEarnings: Double,
    uberRides: Int,
    rapidoEarnings: Double,
    rapidoRides: Int
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL FLEET REVENUE (ALL APPS)",
                    color = TextSecondary,
                    fontSize = 11.sp,
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

            Spacer(modifier = Modifier.height(6.dp))

            EarningsText(amount = totalEarnings)

            Spacer(modifier = Modifier.height(14.dp))

            // Platform Split (Uber vs Rapido)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark.copy(alpha = 0.6f), shape = TileShape)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlatformChip(platform = Platform.UBER)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = currencyFormat.format(uberEarnings),
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$uberRides trips",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(SurfaceBorderDark)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlatformChip(platform = Platform.RAPIDO)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = currencyFormat.format(rapidoEarnings),
                            color = EarningsGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$rapidoRides trips",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Total Rides", value = "$totalRides")
                StatItem(label = "Total Km", value = "${String.format(Locale.US, "%.1f", totalKm)} km")
                StatItem(
                    label = "Active Fleet",
                    value = "$activeCars / 30 Cabs",
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
                .padding(12.dp)
        ) {
            // Reg No & Status Dot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vehicle.regNo,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                StatusDot(state = statusState, size = 8.dp)
            }

            // Nickname
            if (!vehicle.nickname.isNullOrBlank()) {
                Text(
                    text = vehicle.nickname,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Assigned Driver Name (Crucial for owner)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (vehicle.currentDriverName != null) EmeraldAccentContainer.copy(alpha = 0.5f) else SurfaceDark,
                        shape = ChipShape
                    )
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = vehicle.currentDriverName?.let { "👤 $it" } ?: "Idle / Unassigned",
                    color = if (vehicle.currentDriverName != null) EmeraldAccent else TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Combined Total Fare
            Text(
                text = currencyFormat.format(vehicle.todayFare),
                color = EarningsGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Platform Split (Uber vs Rapido)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "U: ₹${vehicle.uberFare.toInt()} (${vehicle.uberRides})",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
                Text(
                    text = "R: ₹${vehicle.rapidoFare.toInt()} (${vehicle.rapidoRides})",
                    color = EarningsGoldLight,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${vehicle.todayRides} trips",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", vehicle.todayKm)} km",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun DriverPerformanceTile(
    driver: DriverPerformance
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    GlassCard(
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
                Column {
                    Text(
                        text = "👤 ${driver.driverName}",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = driver.phone,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = currencyFormat.format(driver.todayFare),
                    color = EarningsGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Assigned Cab Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, shape = TileShape)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = if (driver.assignedVehicleRegNo != null) EmeraldAccent else TextMuted,
                        modifier = Modifier.height(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = driver.assignedVehicleRegNo?.let { "$it (${driver.assignedVehicleNickname})" } ?: "No Cab Assigned Today",
                        color = if (driver.assignedVehicleRegNo != null) TextPrimary else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (driver.isShiftActive) EmeraldAccentContainer else AlertRed.copy(alpha = 0.2f),
                            ChipShape
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (driver.isShiftActive) "Active Shift" else "Off Duty",
                        color = if (driver.isShiftActive) EmeraldAccent else AlertRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Platform Split (Uber vs Rapido)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Uber: ${driver.uberRides} trips (${currencyFormat.format(driver.uberFare)})",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "Rapido: ${driver.rapidoRides} trips (${currencyFormat.format(driver.rapidoFare)})",
                    color = EarningsGold,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Trips: ${driver.todayRides}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = "Distance: ${String.format(Locale.US, "%.1f", driver.todayKm)} km",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
