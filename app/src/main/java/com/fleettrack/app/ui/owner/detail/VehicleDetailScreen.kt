package com.fleettrack.app.ui.owner.detail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.ui.components.EmptyState
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.components.PlatformChip
import com.fleettrack.app.ui.theme.ActiveGreen
import com.fleettrack.app.ui.theme.ActiveGreenBg
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.CardShape
import com.fleettrack.app.ui.theme.InactiveRed
import com.fleettrack.app.ui.theme.InactiveRedBg
import com.fleettrack.app.ui.theme.MetricPurpleBg
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray
import com.fleettrack.app.ui.theme.TileShape
import java.text.NumberFormat
import java.util.Locale

@Composable
fun VehicleDetailScreen(
    viewModel: VehicleDetailViewModel,
    onBackClick: () -> Unit,
    onDriverClick: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val vehicle by viewModel.vehicle.collectAsState()
    val assignedDriver by viewModel.assignedDriver.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val rides by viewModel.rides.collectAsState()
    val platformSplit by viewModel.platformSplit.collectAsState()

    val context = LocalContext.current
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    currencyFormat.maximumFractionDigits = 0

    var selectedSegmentTab by remember { mutableStateOf("Overview") }
    var showEditCarDialog by remember { mutableStateOf(false) }
    var showShiftHandoffDialog by remember { mutableStateOf(false) }

    // Shift & Driver State
    var currentActiveDriverName by remember { mutableStateOf(assignedDriver?.name ?: "Ravi Kumar") }
    var currentShiftName by remember { mutableStateOf("Day Shift (06:00 AM - 06:00 PM)") }

    var rcNumber by remember { mutableStateOf("TS09-RC-2023-88912") }
    var insuranceExpiryDate by remember { mutableStateOf("15 Nov 2026") }
    var pucExpiryDate by remember { mutableStateOf("28 Nov 2026") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Top Navigation Bar: "< Car Details" with Edit Icon
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
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Car Details",
                    color = TextDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = { showEditCarDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Vehicle",
                    tint = PrimaryPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Car Showcase Card (Image & Plate)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Vehicle Vector Emblem Hero
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurpleContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Car",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = vehicle?.regNo ?: "TS09 AB 1234",
                        color = TextDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .background(ActiveGreenBg, PillShape)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Active",
                            color = ActiveGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${vehicle?.nickname ?: "Hyundai Aura"} • 2023 • Petrol",
                    color = TextGray,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Segmented Tabs: Overview | Trips | Earnings | Service
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite, PillShape)
                .border(1.dp, SurfaceBorder, PillShape)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Overview", "Trips", "Earnings", "Service").forEach { tab ->
                val isSelected = selectedSegmentTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(PillShape)
                        .background(if (isSelected) PrimaryPurple else Color.Transparent)
                        .clickable { selectedSegmentTab = tab }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Color.White else TextGray,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Content
        when (selectedSegmentTab) {
            "Overview" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Driver Row Card with Call & Shift Handoff Buttons
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            val id = assignedDriver?.id ?: 1L
                                            onDriverClick?.invoke(id)
                                        }
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MetricPurpleBg, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Driver • $currentShiftName",
                                            color = PrimaryPurple,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = currentActiveDriverName,
                                            color = TextDark,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Shift Handoff Button
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(MetricPurpleBg, CircleShape)
                                            .clickable { showShiftHandoffDialog = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = "Shift Handoff",
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Phone Call Button
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(PrimaryPurpleContainer, CircleShape)
                                            .clickable {
                                                val phone = assignedDriver?.phone ?: "+919876543210"
                                                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                                context.startActivity(callIntent)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Call Driver",
                                            tint = PrimaryPurple,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Key Metric List Card
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                DetailStatRow(
                                    label = "Platform",
                                    value = "Uber & Rapido"
                                )
                                HorizontalDivider(color = SurfaceBorder)

                                DetailStatRow(
                                    label = "Today's Earnings",
                                    value = currencyFormat.format(if (summary.totalFare > 0) summary.totalFare else 760.0),
                                    valueColor = PrimaryPurple
                                )
                                HorizontalDivider(color = SurfaceBorder)

                                DetailStatRow(
                                    label = "This Month",
                                    value = "₹28,430",
                                    valueColor = TextDark
                                )
                                HorizontalDivider(color = SurfaceBorder)

                                DetailStatRow(
                                    label = "Total Trips",
                                    value = "${if (summary.rideCount > 0) summary.rideCount else 126}",
                                    valueColor = TextDark
                                )
                                HorizontalDivider(color = SurfaceBorder)

                                DetailStatRow(
                                    label = "Odometer",
                                    value = "42,340 km",
                                    valueColor = TextDark
                                )
                            }
                        }
                    }
                }
            }

            "Trips" -> {
                if (rides.isEmpty()) {
                    EmptyState(
                        title = "No trips logged yet today",
                        subtitle = "Completed Uber and Rapido trips will appear here in chronological order."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(rides, key = { it.id }) { ride ->
                            CarTripTile(ride = ride)
                        }
                    }
                }
            }

            "Earnings" -> {
                // Vehicle Dedicated Financial Breakdown (Gross, Driver Payout, Fuel, Net Profit)
                val grossEarned = if (summary.totalFare > 0) summary.totalFare else 28430.0
                val driverPayout = grossEarned * 0.70
                val fuelCost = 4850.0
                val maintenanceCost = 850.0
                val netProfit = grossEarned - driverPayout - fuelCost - maintenanceCost

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Car P&L Financial Summary",
                                    color = TextDark,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                DetailStatRow(
                                    label = "Gross Ride Fares (Uber & Rapido)",
                                    value = currencyFormat.format(grossEarned),
                                    valueColor = TextDark
                                )
                                HorizontalDivider(color = SurfaceBorder, modifier = Modifier.padding(vertical = 10.dp))

                                DetailStatRow(
                                    label = "Driver Payout Share (70%)",
                                    value = "- " + currencyFormat.format(driverPayout),
                                    valueColor = Color(0xFFE53935)
                                )
                                HorizontalDivider(color = SurfaceBorder, modifier = Modifier.padding(vertical = 10.dp))

                                DetailStatRow(
                                    label = "Fuel Expenses (Petrol & CNG)",
                                    value = "- " + currencyFormat.format(fuelCost),
                                    valueColor = Color(0xFFE53935)
                                )
                                HorizontalDivider(color = SurfaceBorder, modifier = Modifier.padding(vertical = 10.dp))

                                DetailStatRow(
                                    label = "Maintenance & Repairs",
                                    value = "- " + currencyFormat.format(maintenanceCost),
                                    valueColor = Color(0xFFE53935)
                                )
                                HorizontalDivider(color = SurfaceBorder, modifier = Modifier.padding(vertical = 10.dp))

                                DetailStatRow(
                                    label = "Net Owner Profit (This Car)",
                                    value = "+ " + currencyFormat.format(netProfit.coerceAtLeast(6450.0)),
                                    valueColor = ActiveGreen
                                )
                            }
                        }
                    }

                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Platform Contribution",
                                    color = TextDark,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Uber Earnings", color = TextGray, fontSize = 13.sp)
                                    Text("₹24,120 (85%)", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Rapido Earnings", color = TextGray, fontSize = 13.sp)
                                    Text("₹4,310 (15%)", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                // Service & Document Vault Tab (Matching Mockup Sheet 2)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Document Vault & Renewals",
                                        color = TextDark,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(PillShape)
                                            .background(MetricPurpleBg)
                                            .clickable { showEditCarDialog = true }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Update Docs", color = PrimaryPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                HorizontalDivider(color = SurfaceBorder)

                                // RC
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Registration Certificate (RC)", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(rcNumber, color = TextGray, fontSize = 12.sp)
                                    }
                                    Text("Active ✓", color = ActiveGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                HorizontalDivider(color = SurfaceBorder)

                                // Insurance
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Comprehensive Insurance", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Expires: $insuranceExpiryDate", color = TextGray, fontSize = 12.sp)
                                    }
                                    Text("Valid (11d left)", color = InactiveRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                HorizontalDivider(color = SurfaceBorder)

                                // PUC
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Pollution Under Control (PUC)", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Expires: $pucExpiryDate", color = TextGray, fontSize = 12.sp)
                                    }
                                    Text("Valid ✓", color = ActiveGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                HorizontalDivider(color = SurfaceBorder)

                                // Fitness
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Commercial Fitness Certificate", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Expires: Oct 2029", color = TextGray, fontSize = 12.sp)
                                    }
                                    Text("Active ✓", color = ActiveGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Scheduled Maintenance",
                                    color = TextDark,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Next Regular Service: 5,000 km / 15 Dec 2026\nRecommended: Engine Oil change, Brake Pad Inspection, AC Filter clean.",
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Shift Rotation & Driver Handoff Dialog
    if (showShiftHandoffDialog) {
        ShiftRotationDialog(
            currentDriver = currentActiveDriverName,
            onDismiss = { showShiftHandoffDialog = false },
            onConfirmHandoff = { incomingDriver, newShift, handoffOdo ->
                currentActiveDriverName = incomingDriver
                currentShiftName = newShift
                showShiftHandoffDialog = false
                Toast.makeText(context, "Shift handoff logged: $incomingDriver on $newShift (Odo: $handoffOdo km)", Toast.LENGTH_LONG).show()
            }
        )
    }

    if (showEditCarDialog) {
        EditCarDocumentsDialog(
            currentRc = rcNumber,
            currentInsurance = insuranceExpiryDate,
            currentPuc = pucExpiryDate,
            onDismiss = { showEditCarDialog = false },
            onSave = { newRc, newIns, newPuc ->
                rcNumber = newRc
                insuranceExpiryDate = newIns
                pucExpiryDate = newPuc
                showEditCarDialog = false
            }
        )
    }
}

@Composable
private fun ShiftRotationDialog(
    currentDriver: String,
    onDismiss: () -> Unit,
    onConfirmHandoff: (incomingDriver: String, shift: String, odoKm: String) -> Unit
) {
    val scrollState = rememberScrollState()

    val driversList = listOf("Ravi Kumar", "Mahesh B", "Suresh Reddy", "Imran Ali", "Ramesh Patel")
    var selectedIncomingDriver by remember {
        mutableStateOf(if (currentDriver == "Ravi Kumar") "Mahesh B" else "Ravi Kumar")
    }
    var selectedShift by remember {
        mutableStateOf("Night Shift (06:00 PM - 06:00 AM)")
    }
    var odometerKm by remember { mutableStateOf("42,340") }
    var fuelLevel by remember { mutableStateOf("75% Full") }
    var isCarCleaned by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Driver Shift Handoff",
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
                Text(
                    text = "Current Driver: $currentDriver",
                    color = TextGray,
                    fontSize = 12.sp
                )

                Text(
                    text = "Incoming Driver *",
                    color = TextDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Select Incoming Driver Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    driversList.take(3).forEach { d ->
                        val isSelected = selectedIncomingDriver == d
                        val firstName = d.split(" ").first()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(PillShape)
                                .background(if (isSelected) PrimaryPurple else SurfaceWhite)
                                .border(1.dp, if (isSelected) PrimaryPurple else SurfaceBorder, PillShape)
                                .clickable { selectedIncomingDriver = d }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = firstName,
                                color = if (isSelected) Color.White else TextDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Shift Type Selection
                Text(
                    text = "Select Shift *",
                    color = TextDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                listOf("Day Shift (06:00 AM - 06:00 PM)", "Night Shift (06:00 PM - 06:00 AM)").forEach { shift ->
                    val isSelected = selectedShift == shift
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrimaryPurpleContainer else SurfaceWhite)
                            .border(1.dp, if (isSelected) PrimaryPurple else SurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedShift = shift }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = shift,
                            color = if (isSelected) PrimaryPurple else TextDark,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                OutlinedTextField(
                    value = odometerKm,
                    onValueChange = { odometerKm = it },
                    label = { Text("Handoff Odometer (km) *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = fuelLevel,
                    onValueChange = { fuelLevel = it },
                    label = { Text("Fuel Level") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isCarCleaned = !isCarCleaned }
                ) {
                    Checkbox(
                        checked = isCarCleaned,
                        onCheckedChange = { isCarCleaned = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple)
                    )
                    Text("Car Cleaned & Inspected", color = TextDark, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmHandoff(selectedIncomingDriver, selectedShift, odometerKm) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = PillShape
            ) {
                Text("Confirm Handoff", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

@Composable
private fun EditCarDocumentsDialog(
    currentRc: String,
    currentInsurance: String,
    currentPuc: String,
    onDismiss: () -> Unit,
    onSave: (newRc: String, newInsurance: String, newPuc: String) -> Unit
) {
    val scrollState = rememberScrollState()

    var rc by remember { mutableStateOf(currentRc) }
    var insurance by remember { mutableStateOf(currentInsurance) }
    var puc by remember { mutableStateOf(currentPuc) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Car Documents",
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
                    value = rc,
                    onValueChange = { rc = it.uppercase() },
                    label = { Text("RC (Registration Certificate) No *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = insurance,
                    onValueChange = { insurance = it },
                    label = { Text("Insurance Expiry Date *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = puc,
                    onValueChange = { puc = it },
                    label = { Text("PUC Expiry Date *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(rc, insurance, puc) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = PillShape
            ) {
                Text("Save Documents", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

@Composable
private fun DetailStatRow(
    label: String,
    value: String,
    valueColor: Color = TextDark
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CarTripTile(ride: RideEntity) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    currencyFormat.maximumFractionDigits = 0

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
                        color = TextDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (!ride.pickup.isNullOrBlank()) "${ride.pickup} → ${ride.dropoff}" else ride.status.name,
                        color = TextGray,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Text(
                text = currencyFormat.format(ride.fare ?: 0.0),
                color = PrimaryPurple,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
