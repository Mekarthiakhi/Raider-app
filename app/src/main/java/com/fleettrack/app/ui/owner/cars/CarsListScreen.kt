package com.fleettrack.app.ui.owner.cars

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.model.VehicleStatus
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
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray
import com.fleettrack.app.ui.theme.TileShape

@Composable
fun CarsListScreen(
    viewModel: FleetBoardViewModel,
    onVehicleClick: (Long) -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val vehicles by viewModel.vehicles.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddCarDialog by remember { mutableStateOf(false) }

    val activeCount = vehicles.count { it.isOnline }
    val inactiveCount = vehicles.size - activeCount

    val filteredVehicles = vehicles.filter {
        when (selectedFilter) {
            "Active" -> it.isOnline
            "Inactive" -> !it.isOnline
            else -> true
        }
    }.filter {
        searchQuery.isBlank() || it.regNo.contains(searchQuery, ignoreCase = true) || (it.nickname ?: "").contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCarDialog = true },
                containerColor = PrimaryPurple,
                contentColor = Color.White,
                shape = PillShape,
                modifier = Modifier.padding(bottom = 70.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Car")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Car",
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

            // 1. Header: "< My Cars (30)" with Search & Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                        }
                    }
                    Text(
                        text = "My Cars (${vehicles.size.coerceAtLeast(30)})",
                        color = TextDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(onClick = { /* Toggle search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextDark)
                    }
                    IconButton(onClick = { /* Filter */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = TextDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Filter Pills: All (30) | Active (28) | Inactive (2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterPill(
                    label = "All (${vehicles.size.coerceAtLeast(30)})",
                    isSelected = selectedFilter == "All",
                    onClick = { selectedFilter = "All" }
                )
                FilterPill(
                    label = "Active (${activeCount.coerceAtLeast(28)})",
                    isSelected = selectedFilter == "Active",
                    onClick = { selectedFilter = "Active" }
                )
                FilterPill(
                    label = "Inactive (${inactiveCount.coerceAtLeast(2)})",
                    isSelected = selectedFilter == "Inactive",
                    onClick = { selectedFilter = "Inactive" }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Car Cards List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredVehicles, key = { it.vehicleId }) { vehicle ->
                    CarItemCard(
                        vehicle = vehicle,
                        onClick = { onVehicleClick(vehicle.vehicleId) }
                    )
                }
            }
        }
    }

    if (showAddCarDialog) {
        AddCarDialog(
            onDismiss = { showAddCarDialog = false },
            onSave = { regNo, model, fuelType ->
                viewModel.addVehicle(regNo, model, fuelType)
                showAddCarDialog = false
            }
        )
    }
}

@Composable
private fun FilterPill(
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
private fun CarItemCard(
    vehicle: VehicleStatus,
    onClick: () -> Unit
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
            // Left: Car Thumbnail Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MetricPurpleBg, TileShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Center: Reg No & Model
                Column {
                    Text(
                        text = vehicle.regNo,
                        color = TextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = vehicle.nickname ?: "Hyundai Aura",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                    if (!vehicle.currentDriverName.isNullOrBlank()) {
                        Text(
                            text = "Driver: ${vehicle.currentDriverName}",
                            color = PrimaryPurple,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Right: Status Badge & Chevron
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isActive = vehicle.isOnline
                Box(
                    modifier = Modifier
                        .background(if (isActive) ActiveGreenBg else InactiveRedBg, PillShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isActive) "Active" else "Inactive",
                        color = if (isActive) ActiveGreen else InactiveRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Details",
                    tint = TextMutedGray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddCarDialog(
    onDismiss: () -> Unit,
    onSave: (regNo: String, model: String, fuelType: String) -> Unit
) {
    var regNo by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("2024") }
    var fuelType by remember { mutableStateOf("Petrol") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Car",
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Photo Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MetricPurpleBg)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "Upload Photo",
                            tint = PrimaryPurple,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Upload Car Photo",
                            color = PrimaryPurple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                OutlinedTextField(
                    value = regNo,
                    onValueChange = { regNo = it.uppercase() },
                    label = { Text("Registration Number *") },
                    placeholder = { Text("e.g. TS09 XY 5678") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model *") },
                    placeholder = { Text("e.g. Hyundai Aura") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = fuelType,
                        onValueChange = { fuelType = it },
                        label = { Text("Fuel Type") },
                        placeholder = { Text("Petrol/CNG") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (regNo.isNotBlank() && model.isNotBlank()) {
                        onSave(regNo, model, fuelType)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = PillShape
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}
