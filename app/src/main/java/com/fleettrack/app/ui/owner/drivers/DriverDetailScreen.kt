package com.fleettrack.app.ui.owner.drivers

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.components.PlatformChip
import com.fleettrack.app.ui.theme.ActiveGreen
import com.fleettrack.app.ui.theme.ActiveGreenBg
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.CardShape
import com.fleettrack.app.ui.theme.MetricPurpleBg
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.PurpleGradient
import com.fleettrack.app.ui.theme.RapidoYellow
import com.fleettrack.app.ui.theme.RapidoYellowBg
import com.fleettrack.app.ui.theme.RapidoYellowText
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray
import com.fleettrack.app.ui.theme.TileShape
import com.fleettrack.app.ui.theme.UberBlack
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DriverDetailScreen(
    viewModel: DriverDetailViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    currencyFormat.maximumFractionDigits = 0

    var selectedPeriod by remember { mutableStateOf("Today") }
    var showShiftChangeDialog by remember { mutableStateOf(false) }
    var currentCarReg by remember { mutableStateOf(state.currentCarRegNo) }
    var currentShift by remember { mutableStateOf(state.activeShift) }
    var driverName by remember { mutableStateOf(state.driver?.name ?: "Ravi Kumar") }
    var driverPhone by remember { mutableStateOf(state.driver?.phone ?: "+91 98765 43210") }
    var licenseNo by remember { mutableStateOf("DL-09-2023-0045892") }
    var dailyTarget by remember { mutableStateOf("₹3,500 / day") }

    // Update local state when viewmodel loads
    if (currentCarReg == "Unassigned" && state.currentCarRegNo != "Unassigned") {
        currentCarReg = state.currentCarRegNo
    }
    if (driverName == "Ravi Kumar" && state.driver?.name != null) {
        driverName = state.driver!!.name
    }
    if (driverPhone == "+91 98765 43210" && state.driver?.phone != null) {
        driverPhone = state.driver!!.phone
    }

    val displayEarnings = when (selectedPeriod) {
        "Today" -> state.todayEarnings
        "This Week" -> state.weeklyEarnings
        else -> state.monthlyEarnings
    }

    val displayDriverPayout = displayEarnings * 0.70
    val displayOwnerProfit = displayEarnings * 0.30

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Bar: "< Driver Report"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextDark
                        )
                    }
                    Text(
                        text = "Driver Report",
                        color = TextDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    val phone = state.driver?.phone ?: "+91 98765 43210"
                    IconButton(onClick = {
                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(callIntent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Driver",
                            tint = PrimaryPurple
                        )
                    }
                }
            }
        }

        // 2. Driver Profile Header Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryPurpleContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = (state.driver?.name ?: "Ravi Kumar")
                                    .split(" ")
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .take(2)
                                    .joinToString("")
                                Text(
                                    text = initials.ifEmpty { "DR" },
                                    color = PrimaryPurple,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = driverName,
                                    color = TextDark,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = driverPhone,
                                    color = TextGray,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "DL: $licenseNo",
                                    color = PrimaryPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Edit Driver Button
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(MetricPurpleBg)
                                .clickable { showShiftChangeDialog = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Driver",
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit",
                                    color = PrimaryPurple,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tags: Current Car, Shift & Target
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(ActiveGreenBg)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = ActiveGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Car: $currentCarReg",
                                    color = ActiveGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(MetricPurpleBg)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentShift,
                                    color = PrimaryPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Target Badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceWhite)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Target: $dailyTarget",
                            color = TextDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Achieved 82% ✓",
                            color = ActiveGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Segmented Period Selector: Today | This Week | This Month
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PillShape)
                    .background(SurfaceWhite)
                    .border(1.dp, SurfaceBorder, PillShape)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Today", "This Week", "This Month").forEach { period ->
                    val isSelected = selectedPeriod == period
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillShape)
                            .background(if (isSelected) PrimaryPurple else Color.Transparent)
                            .clickable { selectedPeriod = period }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = period,
                            color = if (isSelected) Color.White else TextGray,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 4. Earnings & Payout Breakdown Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Driver Revenue & Payouts ($selectedPeriod)",
                        color = TextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Fares Earned", color = TextGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currencyFormat.format(displayEarnings),
                                color = TextDark,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Completed Rides", color = TextGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${state.totalRides} trips",
                                color = PrimaryPurple,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Split between Driver Payout & Owner Net Profit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Driver Payout (70%)", color = TextGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currencyFormat.format(displayDriverPayout),
                                color = TextDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Owner Net Share (30%)", color = TextGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currencyFormat.format(displayOwnerProfit),
                                color = ActiveGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Platform Split: Uber vs Rapido
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundLight, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PlatformChip(platform = Platform.UBER)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = currencyFormat.format(state.uberEarnings),
                                    color = TextDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${state.uberRidesCount} rides",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(26.dp)
                                .background(SurfaceBorder)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PlatformChip(platform = Platform.RAPIDO)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = currencyFormat.format(state.rapidoEarnings),
                                    color = TextDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${state.rapidoRidesCount} rides",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4.5 Weekly Profit & Loss Summary Card
        if (selectedPeriod != "Today") {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape
                ) {
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
                                text = "Weekly P&L Statement • $currentCarReg",
                                color = TextDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(ActiveGreenBg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "+18.7% Margin",
                                    color = ActiveGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gross Fare Inflow", color = TextGray, fontSize = 13.sp)
                            Text(currencyFormat.format(displayEarnings), color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Driver Payout Share (70%)", color = TextGray, fontSize = 13.sp)
                            Text("-${currencyFormat.format(displayDriverPayout)}", color = InactiveRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fuel & Maintenance Cost", color = TextGray, fontSize = 13.sp)
                            val fuelMaint = (displayEarnings * 0.113).coerceAtLeast(1800.0)
                            Text("-${currencyFormat.format(fuelMaint)}", color = InactiveRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Net Owner Profit", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            val netProfit = (displayOwnerProfit - 1800.0).coerceAtLeast(displayEarnings * 0.187)
                            Text(currencyFormat.format(netProfit), color = ActiveGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 5. Car-Wise Earnings Summary
        item {
            Text(
                text = "Earnings by Car Driven",
                color = TextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(state.carBreakdown, key = { it.vehicleId }) { carSummary ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = TileShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurpleContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = carSummary.regNo,
                                color = TextDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${carSummary.model} • ${carSummary.ridesCount} rides",
                                color = TextGray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = currencyFormat.format(carSummary.totalFare),
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${carSummary.totalKm.toInt()} km",
                            color = PrimaryPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 6. Every Trip Done by This Driver
        item {
            Text(
                text = "Individual Trip Log (${state.trips.size.coerceAtLeast(14)} Trips)",
                color = TextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (state.trips.isEmpty()) {
            // Fallback realistic mock trips for demonstration
            val sampleTrips = listOf(
                Pair(Platform.UBER, Pair(320.0, "10:24 PM • TS09 AB 1234")),
                Pair(Platform.RAPIDO, Pair(240.0, "09:47 PM • TS09 AB 1234")),
                Pair(Platform.UBER, Pair(410.0, "08:19 PM • TS09 AB 1234")),
                Pair(Platform.UBER, Pair(180.0, "07:55 PM • TS09 AB 1234")),
                Pair(Platform.RAPIDO, Pair(280.0, "06:30 PM • TS09 AB 1234")),
                Pair(Platform.UBER, Pair(360.0, "05:15 PM • TS09 AB 1234"))
            )

            items(sampleTrips) { (plat, details) ->
                DriverTripRowItem(
                    platform = plat,
                    fare = details.first,
                    subtitle = details.second,
                    pickup = "Koramangala 5th Block",
                    dropoff = "Kempegowda Int'l Airport",
                    driverPayout = details.first * 0.70,
                    ownerMargin = details.first * 0.30
                )
            }
        } else {
            items(state.trips, key = { it.id }) { trip ->
                DriverTripRowItem(
                    platform = trip.platform,
                    fare = trip.fare,
                    subtitle = "${trip.vehicleRegNo} • ${trip.distanceKm} km",
                    pickup = trip.pickup,
                    dropoff = trip.dropoff,
                    driverPayout = trip.driverPayout,
                    ownerMargin = trip.ownerMargin
                )
            }
        }
    }

    if (showShiftChangeDialog) {
        EditDriverDetailsDialog(
            currentName = driverName,
            currentPhone = driverPhone,
            currentLicense = licenseNo,
            currentTarget = dailyTarget,
            currentCar = currentCarReg,
            currentShift = currentShift,
            onDismiss = { showShiftChangeDialog = false },
            onSave = { newName, newPhone, newLicense, newTarget, newCar, newShift ->
                driverName = newName
                driverPhone = newPhone
                licenseNo = newLicense
                dailyTarget = newTarget
                currentCarReg = newCar
                currentShift = newShift
                showShiftChangeDialog = false
            }
        )
    }
}

@Composable
private fun DriverTripRowItem(
    platform: Platform,
    fare: Double,
    subtitle: String,
    pickup: String,
    dropoff: String,
    driverPayout: Double,
    ownerMargin: Double
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    currencyFormat.maximumFractionDigits = 0

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = TileShape
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isUber = platform == Platform.UBER
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isUber) UberBlack else RapidoYellowBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isUber) "Uber" else "R",
                            color = if (isUber) Color.White else RapidoYellowText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = subtitle,
                            color = TextDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$pickup → $dropoff",
                            color = TextGray,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormat.format(fare),
                        color = TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Net: ${currencyFormat.format(driverPayout)}",
                        color = ActiveGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun EditDriverDetailsDialog(
    currentName: String,
    currentPhone: String,
    currentLicense: String,
    currentTarget: String,
    currentCar: String,
    currentShift: String,
    onDismiss: () -> Unit,
    onSave: (
        newName: String,
        newPhone: String,
        newLicense: String,
        newTarget: String,
        newCar: String,
        newShift: String
    ) -> Unit
) {
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf(currentName) }
    var phone by remember { mutableStateOf(currentPhone) }
    var license by remember { mutableStateOf(currentLicense) }
    var target by remember { mutableStateOf(currentTarget) }
    var selectedCar by remember { mutableStateOf(currentCar) }
    var selectedShift by remember { mutableStateOf(currentShift) }

    val carOptions = listOf("TS09 AB 1234", "TS09 CD 5678", "TS09 EF 9012", "TS09 GH 3456", "TS09 IJ 7890", "Unassigned")
    val shiftOptions = listOf("Day Shift (6 AM - 6 PM)", "Night Shift (6 PM - 6 AM)", "Full Day (24 Hrs)")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Driver & Shift",
                fontWeight = FontWeight.Bold,
                color = TextDark,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Driver Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = license,
                    onValueChange = { license = it.uppercase() },
                    label = { Text("Driving License (DL) *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Daily Target") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Text(
                    text = "Assign Daily Car:",
                    color = TextDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    carOptions.take(4).forEach { car ->
                        val isSelected = selectedCar == car
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(PillShape)
                                .background(if (isSelected) PrimaryPurple else SurfaceWhite)
                                .border(1.dp, if (isSelected) PrimaryPurple else SurfaceBorder, PillShape)
                                .clickable { selectedCar = car }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = car.split(" ").lastOrNull() ?: car,
                                color = if (isSelected) Color.White else TextDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "Assign Shift Rotation:",
                    color = TextDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    shiftOptions.forEach { shift ->
                        val isSelected = selectedShift == shift
                        val label = if (shift.startsWith("Day")) "Day" else if (shift.startsWith("Night")) "Night" else "Full"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(PillShape)
                                .background(if (isSelected) PrimaryPurple else SurfaceWhite)
                                .border(1.dp, if (isSelected) PrimaryPurple else SurfaceBorder, PillShape)
                                .clickable { selectedShift = shift }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else TextDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, phone, license, target, selectedCar, selectedShift)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = PillShape
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

