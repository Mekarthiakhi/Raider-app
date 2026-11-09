package com.fleettrack.app.ui.managers

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.fleettrack.app.data.local.entity.DriverEntity
import com.fleettrack.app.data.local.entity.VehicleEntity
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.theme.ActiveGreen
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray

@Composable
fun ManagerDetailScreen(
    managerId: String,
    onNavigateBack: () -> Unit,
    viewModel: ManagersViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(managerId) {
        viewModel.selectManager(managerId)
    }

    val overview by viewModel.selectedManagerOverview.collectAsState()
    val drivers by viewModel.selectedManagerDrivers.collectAsState()
    val vehicles by viewModel.selectedManagerVehicles.collectAsState()
    val unassignedDrivers by viewModel.unassignedDrivers.collectAsState()
    val unassignedVehicles by viewModel.unassignedVehicles.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAssignDriverDialog by remember { mutableStateOf(false) }
    var showAssignVehicleDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextDark
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = overview?.managerName ?: "Manager Details",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = overview?.hubName ?: "Hub Management",
                            fontSize = 12.sp,
                            color = PrimaryPurple,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (overview != null) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${overview!!.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9))
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = ActiveGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Stats Card
            if (overview != null) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${overview!!.totalDrivers}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryPurple
                                )
                                Text("Drivers", fontSize = 11.sp, color = TextGray)
                            }
                            Box(modifier = Modifier.width(1.dp).height(28.dp).background(SurfaceBorder))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${overview!!.totalVehicles}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7)
                                )
                                Text("Cabs", fontSize = 11.sp, color = TextGray)
                            }
                            Box(modifier = Modifier.width(1.dp).height(28.dp).background(SurfaceBorder))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "₹${overview!!.todayEarnings.toInt()}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ActiveGreen
                                )
                                Text("Earnings", fontSize = 11.sp, color = TextGray)
                            }
                            Box(modifier = Modifier.width(1.dp).height(28.dp).background(SurfaceBorder))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${overview!!.todayTrips}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEAB308)
                                )
                                Text("Trips", fontSize = 11.sp, color = TextGray)
                            }
                        }
                    }
                }
            }

            // Tabs: Drivers vs Vehicles
            item {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = SurfaceWhite,
                    contentColor = PrimaryPurple,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = PrimaryPurple
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                text = "Assigned Drivers (${drivers.size})",
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                text = "Assigned Cabs (${vehicles.size})",
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Tab 0: Drivers Content
            if (selectedTabIndex == 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ASSIGNED DRIVERS (${drivers.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMutedGray,
                            letterSpacing = 0.8.sp
                        )
                        Button(
                            onClick = { showAssignDriverDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = PillShape,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Assign Driver", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (drivers.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "No drivers assigned yet. Tap '+ Assign Driver' to allocate from unassigned pool.",
                                fontSize = 12.sp,
                                color = TextMutedGray,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }
                } else {
                    items(drivers, key = { it.id }) { driver ->
                        DriverItemRow(
                            driver = driver,
                            onRemove = { viewModel.assignDriver(driver.id, null) },
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${driver.phone}"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }

            // Tab 1: Vehicles Content
            if (selectedTabIndex == 1) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ASSIGNED CABS (${vehicles.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMutedGray,
                            letterSpacing = 0.8.sp
                        )
                        Button(
                            onClick = { showAssignVehicleDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = PillShape,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Assign Cab", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (vehicles.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "No vehicles assigned yet. Tap '+ Assign Cab' to allocate from unassigned pool.",
                                fontSize = 12.sp,
                                color = TextMutedGray,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }
                } else {
                    items(vehicles, key = { it.id }) { vehicle ->
                        VehicleItemRow(
                            vehicle = vehicle,
                            onRemove = { viewModel.assignVehicle(vehicle.id, null) }
                        )
                    }
                }
            }
        }
    }

    // Dialog: Assign Driver from Pool
    if (showAssignDriverDialog) {
        AlertDialog(
            onDismissRequest = { showAssignDriverDialog = false },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("Assign Driver to ${overview?.managerName ?: "Manager"}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            },
            text = {
                if (unassignedDrivers.isEmpty()) {
                    Text("No unassigned drivers available. All 30 drivers are allocated to hubs.", fontSize = 12.sp, color = TextGray)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(unassignedDrivers, key = { it.id }) { d ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF8F9FA))
                                    .clickable {
                                        viewModel.assignDriver(d.id, managerId)
                                        showAssignDriverDialog = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(d.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                                    Text(d.phone, fontSize = 11.sp, color = TextGray)
                                }
                                Text("Assign ➔", color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAssignDriverDialog = false }) {
                    Text("Close", color = TextMutedGray)
                }
            }
        )
    }

    // Dialog: Assign Vehicle from Pool
    if (showAssignVehicleDialog) {
        AlertDialog(
            onDismissRequest = { showAssignVehicleDialog = false },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("Assign Cab to ${overview?.managerName ?: "Manager"}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            },
            text = {
                if (unassignedVehicles.isEmpty()) {
                    Text("No unassigned cabs available. All 30 vehicles are allocated to hubs.", fontSize = 12.sp, color = TextGray)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(unassignedVehicles, key = { it.id }) { v ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF8F9FA))
                                    .clickable {
                                        viewModel.assignVehicle(v.id, managerId)
                                        showAssignVehicleDialog = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(v.regNo, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                                    Text(v.nickname ?: "Cab", fontSize = 11.sp, color = TextGray)
                                }
                                Text("Assign ➔", color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAssignVehicleDialog = false }) {
                    Text("Close", color = TextMutedGray)
                }
            }
        )
    }
}

@Composable
private fun DriverItemRow(
    driver: DriverEntity,
    onRemove: () -> Unit,
    onCall: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurpleContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.People, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(driver.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                Text(driver.phone, fontSize = 11.sp, color = TextGray)
            }
            IconButton(onClick = onCall, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Phone, contentDescription = "Call", tint = ActiveGreen, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun VehicleItemRow(
    vehicle: VehicleEntity,
    onRemove: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F2FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(vehicle.regNo, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                Text(vehicle.nickname ?: "Cab", fontSize = 11.sp, color = TextGray)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (vehicle.status == "active") ActiveGreen.copy(alpha = 0.15f) else Color(0xFFF3F4F6))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = vehicle.status.uppercase(),
                    color = if (vehicle.status == "active") ActiveGreen else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
            }
        }
    }
}

