package com.fleettrack.app.ui.owner.maintenance

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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.ui.components.GlassCard
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

data class MaintenanceItem(
    val id: String,
    val regNo: String,
    val title: String,
    val dueDate: String,
    val type: MaintenanceType,
    val cost: Double? = null,
    val isCompleted: Boolean = false
)

enum class MaintenanceType {
    SERVICE,
    INSURANCE,
    PUC,
    FITNESS
}

@Composable
fun MaintenanceScreen(
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Upcoming") }
    var showAddDialog by remember { mutableStateOf(false) }

    val maintenanceItems = remember {
        mutableStateListOf(
            MaintenanceItem("1", "TS09 AB 1234", "Service Due in 3 days", "05 Nov", MaintenanceType.SERVICE, 4500.0),
            MaintenanceItem("2", "TS09 CD 5678", "Service Due in 7 days", "09 Nov", MaintenanceType.SERVICE, 3200.0),
            MaintenanceItem("3", "TS09 EF 9012", "Insurance Expiry", "20 Nov", MaintenanceType.INSURANCE, 12500.0),
            MaintenanceItem("4", "TS09 GH 3456", "PUC Expiry", "28 Nov", MaintenanceType.PUC, 150.0),
            MaintenanceItem("5", "TS09 IJ 7890", "Wheel Alignment & Balancing", "30 Nov", MaintenanceType.SERVICE, 1800.0)
        )
    }

    val historyItems = remember {
        mutableStateListOf(
            MaintenanceItem("101", "TS09 AB 1234", "Oil & Filter Change", "15 Oct 2026", MaintenanceType.SERVICE, 3800.0, true),
            MaintenanceItem("102", "TS09 CD 5678", "Brake Pad Replacement", "08 Oct 2026", MaintenanceType.SERVICE, 5200.0, true),
            MaintenanceItem("103", "TS09 KL 2345", "Insurance Renewed", "28 Sep 2026", MaintenanceType.INSURANCE, 11800.0, true)
        )
    }

    val displayedItems = if (selectedTab == "Upcoming") maintenanceItems else historyItems

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryPurple,
                contentColor = Color.White,
                shape = PillShape,
                modifier = Modifier.padding(bottom = 70.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Service")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Service / Expense",
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

            // 1. Top Header: "< Maintenance"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    text = "Maintenance",
                    color = TextDark,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Segmented Tabs: Upcoming (5) | History
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PillShape)
                    .background(SurfaceWhite)
                    .border(1.dp, SurfaceBorder, PillShape)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(PillShape)
                        .background(if (selectedTab == "Upcoming") PrimaryPurple else Color.Transparent)
                        .clickable { selectedTab = "Upcoming" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Upcoming (${maintenanceItems.size})",
                        color = if (selectedTab == "Upcoming") Color.White else TextDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(PillShape)
                        .background(if (selectedTab == "History") PrimaryPurple else Color.Transparent)
                        .clickable { selectedTab = "History" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "History",
                        color = if (selectedTab == "History") Color.White else TextDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Maintenance List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedItems, key = { it.id }) { item ->
                    MaintenanceCardItem(item = item)
                }
            }
        }
    }

    if (showAddDialog) {
        AddServiceDialog(
            onDismiss = { showAddDialog = false },
            onSave = { car, type, date, amount, notes ->
                maintenanceItems.add(
                    0,
                    MaintenanceItem(
                        id = System.currentTimeMillis().toString(),
                        regNo = car,
                        title = "$type - $notes",
                        dueDate = date,
                        type = MaintenanceType.SERVICE,
                        cost = amount
                    )
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun MaintenanceCardItem(item: MaintenanceItem) {
    val (icon, tintBg, tintColor) = when (item.type) {
        MaintenanceType.SERVICE -> Triple(Icons.Default.Build, MetricPurpleBg, PrimaryPurple)
        MaintenanceType.INSURANCE -> Triple(Icons.Default.Security, ActiveGreenBg, ActiveGreen)
        MaintenanceType.PUC -> Triple(Icons.Default.Description, InactiveRedBg, InactiveRed)
        MaintenanceType.FITNESS -> Triple(Icons.Default.Warning, MetricPurpleBg, PrimaryPurple)
    }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(tintBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.title,
                        tint = tintColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = item.regNo,
                        color = TextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.title,
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(PrimaryPurpleContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = item.dueDate,
                    color = PrimaryPurple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AddServiceDialog(
    onDismiss: () -> Unit,
    onSave: (car: String, type: String, date: String, amount: Double, notes: String) -> Unit
) {
    var car by remember { mutableStateOf("TS09 AB 1234") }
    var serviceType by remember { mutableStateOf("Regular Service") }
    var date by remember { mutableStateOf("05 Nov 2026") }
    var amount by remember { mutableStateOf("4500") }
    var notes by remember { mutableStateOf("Oil change, filter, general check") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Service / Maintenance",
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = car,
                    onValueChange = { car = it.uppercase() },
                    label = { Text("Car Registration") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = serviceType,
                    onValueChange = { serviceType = it },
                    label = { Text("Service Type") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (₹)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                    onSave(car, serviceType, date, parsedAmount, notes)
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

