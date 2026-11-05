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
import androidx.compose.material.icons.filled.Person
import android.content.Context
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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

    val context = LocalContext.current
    val sessionPrefs = remember { context.getSharedPreferences("svm_fleet_session", Context.MODE_PRIVATE) }
    val loggedInDriverName = remember { sessionPrefs.getString("driver_name", null) ?: shiftSession.driverName ?: "Driver Partner" }
    val loggedInDriverPhone = remember { sessionPrefs.getString("phone", null) ?: "" }
    val loggedInDriverId = remember { sessionPrefs.getLong("driver_id", 0L) }

    // Lock identity to the authenticated driver - cannot be changed by user
    val currentDriver = remember(availableDrivers, loggedInDriverId, loggedInDriverPhone, loggedInDriverName) {
        availableDrivers.firstOrNull { it.id == loggedInDriverId }
            ?: availableDrivers.firstOrNull { 
                val p = it.phone.filter { ch -> ch.isDigit() }
                val lp = loggedInDriverPhone.filter { ch -> ch.isDigit() }
                p.isNotEmpty() && lp.isNotEmpty() && (p.endsWith(lp) || lp.endsWith(p))
            }
            ?: DriverEntity(
                id = if (loggedInDriverId > 0) loggedInDriverId else 1L,
                fleetId = "svm_fleet_hyderabad",
                name = loggedInDriverName,
                phone = loggedInDriverPhone
            )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen Top Bar: Greet the driver directly & display locked driver credentials
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = "Hi, $loggedInDriverName 👋",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = if (shiftSession.isShiftActive) 
                        "On Duty • ${shiftSession.vehicleRegNo} (${shiftSession.vehicleNickname})" 
                    else 
                        "SVM Driver Partner • ${if (loggedInDriverPhone.isNotBlank()) "+91 $loggedInDriverPhone" else "Moosapet Hub"}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1
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

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Shift Check-In / Active Session Card
        if (!shiftSession.isShiftActive) {
            ShiftCheckInCard(
                assignedDriver = currentDriver,
                vehicles = availableVehicles,
                onStartShift = { driver, vehicle ->
                    viewModel.startShift(driver, vehicle)
                }
            )
        } else {
            ActiveShiftCard(
                driverName = shiftSession.driverName ?: currentDriver.name,
                vehicleRegNo = shiftSession.vehicleRegNo ?: "Unassigned",
                vehicleNickname = shiftSession.vehicleNickname ?: "",
                onEndShift = {
                    viewModel.endShift()
                }
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
private fun SearchableVehicleDialog(
    vehicles: List<VehicleEntity>,
    selectedVehicle: VehicleEntity?,
    onSelectVehicle: (VehicleEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredVehicles = remember(vehicles, searchQuery) {
        if (searchQuery.isBlank()) vehicles
        else vehicles.filter {
            it.regNo.contains(searchQuery, ignoreCase = true) ||
            it.nickname.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = EmeraldAccent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Select Assigned Cab",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search plate e.g. 1840, Dzire, Aura...", fontSize = 12.sp, color = TextMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldAccent,
                        unfocusedBorderColor = SurfaceBorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${filteredVehicles.size} cabs available in Moosapet Fleet",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredVehicles, key = { it.id }) { vehicle ->
                        val isSelected = vehicle.id == selectedVehicle?.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) EmeraldAccent.copy(alpha = 0.15f) else SurfaceDark)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) EmeraldAccent else SurfaceBorderDark,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onSelectVehicle(vehicle)
                                    onDismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) EmeraldAccent.copy(alpha = 0.25f) else BackgroundDark),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = if (isSelected) EmeraldAccent else TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f, fill = false)) {
                                        Text(
                                            text = vehicle.regNo,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isSelected) EmeraldAccent else TextPrimary
                                        )
                                        Text(
                                            text = vehicle.nickname,
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = EmeraldAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = BackgroundDark,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun ShiftCheckInCard(
    assignedDriver: DriverEntity,
    vehicles: List<VehicleEntity>,
    onStartShift: (DriverEntity, VehicleEntity) -> Unit
) {
    var selectedVehicle by remember { mutableStateOf<VehicleEntity?>(null) }
    var showVehicleSearchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(vehicles) {
        if (selectedVehicle == null && vehicles.isNotEmpty()) {
            selectedVehicle = vehicles.first()
        }
    }

    if (showVehicleSearchDialog) {
        SearchableVehicleDialog(
            vehicles = vehicles,
            selectedVehicle = selectedVehicle,
            onSelectVehicle = { selectedVehicle = it },
            onDismiss = { showVehicleSearchDialog = false }
        )
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ASSIGNED SHIFT CAB",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Tap to search 30 cabs",
                    color = EmeraldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vehicle Selector with Search Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorderDark, RoundedCornerShape(12.dp))
                    .clickable { showVehicleSearchDialog = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = EarningsGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = selectedVehicle?.regNo ?: "Select Assigned Cab",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (selectedVehicle != null) {
                                Text(
                                    text = selectedVehicle!!.nickname,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = EmeraldAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Search",
                            color = EmeraldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val v = selectedVehicle
                    if (v != null) {
                        onStartShift(assignedDriver, v)
                    }
                },
                enabled = selectedVehicle != null,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = BackgroundDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedVehicle != null) "Start Shift with ${selectedVehicle!!.regNo}" else "Start Shift & Begin Tracking",
                    color = BackgroundDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
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
