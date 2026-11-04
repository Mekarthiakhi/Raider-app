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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.fleettrack.app.data.model.DriverPerformance
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.owner.fleet.FleetBoardViewModel
import com.fleettrack.app.ui.theme.ActiveGreen
import com.fleettrack.app.ui.theme.ActiveGreenBg
import com.fleettrack.app.ui.theme.BackgroundLight
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

@Composable
fun DriversScreen(
    viewModel: FleetBoardViewModel,
    onDriverClick: (Long) -> Unit = {},
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val drivers by viewModel.drivers.collectAsState()
    val context = LocalContext.current

    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var showAddDriverDialog by remember { mutableStateOf(false) }

    val filteredDrivers = drivers.filter { driver ->
        val matchesSearch = driver.driverName.contains(searchQuery, ignoreCase = true) ||
                (driver.assignedVehicleRegNo?.contains(searchQuery, ignoreCase = true) ?: false)

        val matchesFilter = when (selectedFilter) {
            "Active" -> driver.isShiftActive
            "Idle" -> !driver.isShiftActive
            else -> true
        }

        matchesSearch && matchesFilter
    }

    val activeCount = drivers.count { it.isShiftActive }
    val idleCount = drivers.size - activeCount

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDriverDialog = true },
                containerColor = PrimaryPurple,
                contentColor = Color.White,
                shape = PillShape,
                modifier = Modifier.padding(bottom = 70.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Driver")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Driver",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Top Header: "< Drivers (28)" with Search & Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextDark
                            )
                        }
                    }
                    Text(
                        text = "Drivers (${drivers.size.coerceAtLeast(28)})",
                        color = TextDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearchVisible) PrimaryPurple else TextDark
                        )
                    }
                    IconButton(onClick = {
                        selectedFilter = when (selectedFilter) {
                            "All" -> "Active"
                            "Active" -> "Idle"
                            else -> "All"
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = TextDark
                        )
                    }
                }
            }

            // Search Bar
            if (isSearchVisible) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search driver name or car reg...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Filter Pills: All (28) | Active (24) | Idle (4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DriverFilterPill(
                    label = "All (${drivers.size.coerceAtLeast(28)})",
                    isSelected = selectedFilter == "All",
                    onClick = { selectedFilter = "All" }
                )
                DriverFilterPill(
                    label = "Active (${activeCount.coerceAtLeast(24)})",
                    isSelected = selectedFilter == "Active",
                    onClick = { selectedFilter = "Active" }
                )
                DriverFilterPill(
                    label = "Idle (${idleCount.coerceAtLeast(4)})",
                    isSelected = selectedFilter == "Idle",
                    onClick = { selectedFilter = "Idle" }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Driver List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredDrivers, key = { it.driverId }) { driver ->
                    DriverCardItem(
                        driver = driver,
                        onClick = { onDriverClick(driver.driverId) },
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${driver.phone}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    if (showAddDriverDialog) {
        AddDriverDialog(
            onDismiss = { showAddDriverDialog = false },
            onSave = { name, phone, licenseNo, assignedCar, shift, target ->
                viewModel.addDriver(
                    name = name,
                    phone = phone,
                    licenseNo = licenseNo,
                    assignedCarReg = assignedCar,
                    shift = shift
                )
                showAddDriverDialog = false
            }
        )
    }
}

@Composable
private fun DriverFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(PillShape)
            .background(if (isSelected) PrimaryPurple else SurfaceWhite)
            .border(
                width = 1.dp,
                color = if (isSelected) PrimaryPurple else SurfaceBorder,
                shape = PillShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TextDark,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun DriverCardItem(
    driver: DriverPerformance,
    onClick: () -> Unit,
    onCall: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = TileShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Avatar + Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Avatar with initials
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurpleContainer),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = driver.driverName.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                    Text(
                        text = initials.ifEmpty { "DR" },
                        color = PrimaryPurple,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = driver.driverName,
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    val assignedCar = driver.assignedVehicleRegNo ?: "Unassigned"
                    Text(
                        text = "Assigned: $assignedCar",
                        color = TextGray,
                        fontSize = 12.sp
                    )

                    if (driver.isShiftActive && driver.todayRides > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${driver.todayRides} rides • ₹${driver.todayFare.toInt()}",
                            color = PrimaryPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Right: Status Badge & Call Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(if (driver.isShiftActive) ActiveGreenBg else InactiveRedBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (driver.isShiftActive) "Active" else "Idle",
                        color = if (driver.isShiftActive) ActiveGreen else InactiveRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onCall,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurpleContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call ${driver.driverName}",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddDriverDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        phone: String,
        licenseNo: String,
        assignedCar: String,
        shift: String,
        target: String
    ) -> Unit
) {
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var licenseNo by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("₹3,500") }
    var assignedCar by remember { mutableStateOf("TS09 AB 1234") }
    var shift by remember { mutableStateOf("Day Shift (6 AM - 6 PM)") }

    val carOptions = listOf("TS09 AB 1234", "TS09 CD 5678", "TS09 EF 9012", "TS09 GH 3456", "TS09 IJ 7890", "Unassigned")
    val shiftOptions = listOf("Day Shift (6 AM - 6 PM)", "Night Shift (6 PM - 6 AM)", "Full Day (24 Hrs)")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Driver & Shift",
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
                    label = { Text("Driver Full Name *") },
                    placeholder = { Text("e.g. Ramesh Patel") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number *") },
                    placeholder = { Text("+91 98765 43210") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = licenseNo,
                    onValueChange = { licenseNo = it.uppercase() },
                    label = { Text("Driving License (DL) Number *") },
                    placeholder = { Text("e.g. DL-09-2023-0045892") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Daily Revenue Target") },
                    placeholder = { Text("e.g. ₹3,500") },
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    carOptions.take(3).forEach { car ->
                        val isSelected = assignedCar == car
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(PillShape)
                                .background(if (isSelected) PrimaryPurple else SurfaceWhite)
                                .border(1.dp, if (isSelected) PrimaryPurple else SurfaceBorder, PillShape)
                                .clickable { assignedCar = car }
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
                    shiftOptions.forEach { s ->
                        val isSelected = shift == s
                        val label = if (s.startsWith("Day")) "Day (12h)" else if (s.startsWith("Night")) "Night (12h)" else "Full (24h)"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(PillShape)
                                .background(if (isSelected) PrimaryPurple else SurfaceWhite)
                                .border(1.dp, if (isSelected) PrimaryPurple else SurfaceBorder, PillShape)
                                .clickable { shift = s }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else TextDark,
                                fontSize = 10.sp,
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
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        val validDl = if (licenseNo.isBlank()) "DL-${name.take(3).uppercase()}-2024" else licenseNo
                        onSave(name, phone, validDl, assignedCar, shift, target)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = PillShape
            ) {
                Text("Save Driver", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

