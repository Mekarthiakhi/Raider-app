package com.fleettrack.app.ui.manager

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fleettrack.app.data.local.entity.DriverEntity
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
fun ManagerDriversScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManagerDashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val session by viewModel.managerSession.collectAsState()
    val drivers by viewModel.assignedDrivers.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredDrivers = remember(drivers, searchQuery) {
        if (searchQuery.isBlank()) drivers
        else drivers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF0284C7),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Driver")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            text = "My Hub Drivers (${drivers.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = session.hubName ?: "Assigned Hub",
                            fontSize = 12.sp,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or phone...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextMutedGray)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMutedGray)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // List of Drivers
            items(filteredDrivers, key = { it.id }) { driver ->
                DriverCard(
                    driver = driver,
                    onCall = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${driver.phone}"))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        var driverName by remember { mutableStateOf("") }
        var driverPhone by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("Onboard Driver into Team", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "The driver will be immediately assigned to ${session.hubName}.",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    OutlinedTextField(
                        value = driverName,
                        onValueChange = { driverName = it },
                        label = { Text("Driver Full Name") },
                        placeholder = { Text("e.g. Ramesh Reddy") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = driverPhone,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }
                            if (digits.length <= 10) driverPhone = digits
                        },
                        label = { Text("10-Digit Mobile") },
                        placeholder = { Text("e.g. 9845122710") },
                        leadingIcon = { Text("+91 ", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(start = 10.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = Color(0xFFDC2626), fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (driverName.isBlank() || driverPhone.length < 10) {
                            errorMessage = "Please enter valid driver name and 10-digit phone."
                            return@Button
                        }
                        viewModel.onboardDriver(driverName, driverPhone)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = PillShape
                ) {
                    Text("Add to Team", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextMutedGray)
                }
            }
        )
    }
}

@Composable
private fun DriverCard(
    driver: DriverEntity,
    onCall: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F2FE)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = driver.name.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0369A1)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(driver.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                Text(driver.phone, fontSize = 12.sp, color = TextGray)
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ActiveGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (driver.consentAt != null) "Telemetry Active (Uber/Rapido)" else "Standby",
                        fontSize = 10.sp,
                        color = if (driver.consentAt != null) ActiveGreen else TextMutedGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            IconButton(
                onClick = onCall,
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

