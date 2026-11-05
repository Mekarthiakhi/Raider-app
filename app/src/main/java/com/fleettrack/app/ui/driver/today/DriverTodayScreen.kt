package com.fleettrack.app.ui.driver.today

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.local.entity.DriverEntity
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.local.entity.VehicleEntity
import com.fleettrack.app.data.model.Platform
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
import com.fleettrack.app.ui.theme.SurfaceBorderDark
import com.fleettrack.app.ui.theme.SurfaceDark
import com.fleettrack.app.ui.theme.TextMuted
import com.fleettrack.app.ui.theme.TextPrimary
import com.fleettrack.app.ui.theme.TextSecondary
import com.fleettrack.app.ui.theme.TileShape
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DriverTodayScreen(
    viewModel: DriverTodayViewModel,
    onLogoutClick: () -> Unit = {},
    onSetupClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val shiftSession by viewModel.shiftSession.collectAsState()
    val availableDrivers by viewModel.availableDrivers.collectAsState()
    val availableVehicles by viewModel.availableVehicles.collectAsState()
    val isTrackingActive by viewModel.isTrackingActive.collectAsState()
    val summary by viewModel.todaySummary.collectAsState()
    val rides by viewModel.todayRides.collectAsState()
    val platformStats by viewModel.platformBreakdown.collectAsState()

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

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
                    text = if (shiftSession.isShiftActive) (shiftSession.driverName ?: "Driver") else "SVM FLEET Driver",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (shiftSession.isShiftActive) "${shiftSession.vehicleRegNo} (${shiftSession.vehicleNickname})" else "Moosapet Shift Companion",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusDot(
                            state = if (isTrackingActive) VehicleStatusState.ON_TRIP else VehicleStatusState.OFFLINE,
                            size = 6.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTrackingActive) "Active" else "Paused",
                            color = if (isTrackingActive) EmeraldAccent else AlertRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Setup / Permissions
                IconButton(onClick = onSetupClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Setup",
                        tint = TextSecondary
                    )
                }

                // Logout / Exit Button
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Exit to Login",
                        tint = AlertRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Shift Check-In / Active Session Card
        if (!shiftSession.isShiftActive) {
            ShiftCheckInCard(
                drivers = availableDrivers,
                vehicles = availableVehicles,
                onStartShift = { driver, vehicle ->
                    viewModel.startShift(driver, vehicle)
                }
            )
        } else {
            ActiveShiftCard(
                driverName = shiftSession.driverName ?: "Unknown",
                vehicleRegNo = shiftSession.vehicleRegNo ?: "Unassigned",
                vehicleNickname = shiftSession.vehicleNickname ?: "",
                onEndShift = { viewModel.endShift() }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Hero Driver Total Earnings Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "COMBINED SHIFT EARNINGS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                EarningsText(amount = summary.totalFare)
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(label = "Total Trips", value = "${summary.rideCount}")
                    StatItem(
                        label = "Distance",
                        value = "${String.format(Locale.US, "%.1f", summary.totalKm)} km"
                    )
                    StatItem(
                        label = "Avg / Km",
                        value = "₹${String.format(Locale.US, "%.1f", summary.avgFarePerKm)}",
                        valueColor = EmeraldAccent
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Platform Split Breakdown Row (Uber, Rapido, Ola)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark.copy(alpha = 0.6f), shape = TileShape)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Uber
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PlatformChip(platform = Platform.UBER)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = currencyFormat.format(platformStats.uberFare),
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${platformStats.uberRides} trips",
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

                    // Rapido
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PlatformChip(platform = Platform.RAPIDO)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = currencyFormat.format(platformStats.rapidoFare),
                                color = EarningsGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${platformStats.rapidoRides} trips",
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

                    // Ola
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PlatformChip(platform = Platform.OLA)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = currencyFormat.format(platformStats.olaFare),
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${platformStats.olaRides} trips",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Completed Trips Today",
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (rides.isEmpty()) {
            EmptyState(
                title = "No trips recorded for this shift",
                subtitle = "Accept and complete trips in Uber Driver, Rapido Captain, or Ola Driver. Telemetry will automatically track and reconcile amounts."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(rides, key = { it.id }) { ride ->
                    DriverRideTile(ride = ride)
                }
            }
        }
    }
}

@Composable
private fun ShiftCheckInCard(
    drivers: List<DriverEntity>,
    vehicles: List<VehicleEntity>,
    onStartShift: (DriverEntity, VehicleEntity) -> Unit
) {
    var selectedDriver by remember(drivers) { mutableStateOf(drivers.firstOrNull()) }
    var selectedVehicle by remember(vehicles) { mutableStateOf(vehicles.firstOrNull()) }

    var driverDropdownExpanded by remember { mutableStateOf(false) }
    var vehicleDropdownExpanded by remember { mutableStateOf(false) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Shift Check-In",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Select your employee profile and assigned cab to start tracking",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Driver Selector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, shape = TileShape)
                    .border(1.dp, SurfaceBorderDark, TileShape)
                    .clickable { driverDropdownExpanded = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedDriver?.name ?: "Select Driver / Employee",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextMuted)
                }

                DropdownMenu(
                    expanded = driverDropdownExpanded,
                    onDismissRequest = { driverDropdownExpanded = false }
                ) {
                    drivers.forEach { driver ->
                        DropdownMenuItem(
                            text = { Text("${driver.name} (${driver.phone})") },
                            onClick = {
                                selectedDriver = driver
                                driverDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Vehicle Selector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, shape = TileShape)
                    .border(1.dp, SurfaceBorderDark, TileShape)
                    .clickable { vehicleDropdownExpanded = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = EarningsGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedVehicle?.let { "${it.regNo} (${it.nickname})" } ?: "Select Assigned Cab",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextMuted)
                }

                DropdownMenu(
                    expanded = vehicleDropdownExpanded,
                    onDismissRequest = { vehicleDropdownExpanded = false }
                ) {
                    vehicles.forEach { vehicle ->
                        DropdownMenuItem(
                            text = { Text("${vehicle.regNo} - ${vehicle.nickname}") },
                            onClick = {
                                selectedVehicle = vehicle
                                vehicleDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    val d = selectedDriver
                    val v = selectedVehicle
                    if (d != null && v != null) {
                        onStartShift(d, v)
                    }
                },
                enabled = selectedDriver != null && selectedVehicle != null,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = BackgroundDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Shift & Begin Tracking",
                    color = BackgroundDark,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ActiveShiftCard(
    driverName: String,
    vehicleRegNo: String,
    vehicleNickname: String,
    onEndShift: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ACTIVE SHIFT",
                    color = EmeraldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "👤 $driverName",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "🚖 $vehicleRegNo ${if (vehicleNickname.isNotBlank()) "($vehicleNickname)" else ""}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            OutlinedButton(
                onClick = onEndShift,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f))
            ) {
                Text("End Shift", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun DriverRideTile(ride: RideEntity) {
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
                PlatformChip(platform = ride.platform)

                Text(
                    text = currencyFormat.format(ride.fare ?: 0.0),
                    color = EarningsGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!ride.pickup.isNullOrBlank()) {
                Text(
                    text = "📍 ${ride.pickup}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            if (!ride.dropoff.isNullOrBlank()) {
                Text(
                    text = "🏁 ${ride.dropoff}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${ride.distanceKm ?: 0.0} km • ${ride.durationMin?.toInt() ?: 0} min",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = ride.status.name,
                    color = if (ride.status.name == "COMPLETED") EmeraldAccent else AlertRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
