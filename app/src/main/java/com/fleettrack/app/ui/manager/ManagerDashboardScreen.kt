package com.fleettrack.app.ui.manager

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.fleettrack.app.data.local.entity.DriverEntity
import com.fleettrack.app.data.local.entity.DutyLogEntity
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.theme.ActiveGreen
import com.fleettrack.app.ui.theme.ActiveGreenBg
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.InactiveRed
import com.fleettrack.app.ui.theme.InactiveRedBg
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray

@Composable
fun ManagerDashboardScreen(
    onNavigateToDrivers: () -> Unit,
    onNavigateToCars: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToLiveTracking: () -> Unit,
    onBackToOwnerClick: () -> Unit = {},
    onLogoutClick: () -> Unit,
    viewModel: ManagerDashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessionPrefs = remember { context.getSharedPreferences("svm_fleet_session", android.content.Context.MODE_PRIVATE) }
    val isOwnerPreview = remember { sessionPrefs.getBoolean("is_owner_preview", false) }
    val session by viewModel.managerSession.collectAsState()
    val overview by viewModel.managerOverview.collectAsState()
    val drivers by viewModel.assignedDrivers.collectAsState()
    val vehicles by viewModel.assignedVehicles.collectAsState()
    val dutyLogs by viewModel.assignedDutyLogs.collectAsState()

    var showOnboardDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Manager Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PrimaryPurpleContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "📍 ${session.hubName ?: "Hyderabad Hub"}",
                            color = PrimaryPurple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = session.managerName ?: "Hub Manager",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Team Portal • Managing ${drivers.size} Drivers & ${vehicles.size} Cabs",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }

                if (isOwnerPreview) {
                    TextButton(
                        onClick = {
                            viewModel.switchBackToOwner()
                            onBackToOwnerClick()
                        }
                    ) {
                        Text("Owner View", color = PrimaryPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                IconButton(
                    onClick = {
                        viewModel.logout()
                        onLogoutClick()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(InactiveRedBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = InactiveRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Hub Hero Metrics Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "TODAY'S HUB PERFORMANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMutedGray,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "Drivers",
                            value = "${drivers.size}",
                            subtext = "${drivers.count { it.consentAt != null }} On Duty",
                            color = PrimaryPurple
                        )
                        MetricItem(
                            label = "Cabs Covered",
                            value = "${vehicles.size}",
                            subtext = "${vehicles.count { it.status == "active" }} Active",
                            color = Color(0xFF0284C7)
                        )
                        MetricItem(
                            label = "Earnings",
                            value = "₹${(overview?.todayEarnings ?: 0.0).toInt()}",
                            subtext = "Office Settled",
                            color = ActiveGreen
                        )
                        MetricItem(
                            label = "Trips",
                            value = "${overview?.todayTrips ?: 0}",
                            subtext = "Today",
                            color = Color(0xFFEAB308)
                        )
                    }
                }
            }
        }

        // 3. Quick Hub Management Shortcuts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Register Shortcut
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onNavigateToRegister),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEDE7F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Office Register", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                        Text("${dutyLogs.size} Shifts logged", fontSize = 11.sp, color = TextGray)
                    }
                }

                // Live Map Shortcut
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onNavigateToLiveTracking),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Live Tracking", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                        Text("${vehicles.size} Cabs on Map", fontSize = 11.sp, color = TextGray)
                    }
                }
            }
        }

        // 4. Assigned Team Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MY DRIVERS TEAM (${drivers.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMutedGray,
                    letterSpacing = 0.8.sp
                )
                TextButton(onClick = onNavigateToDrivers) {
                    Text("View All ➔", color = Color(0xFF0284C7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 5. Driver List Preview (first 5 drivers)
        items(drivers.take(5), key = { it.id }) { driver ->
            ManagerDriverCard(
                driver = driver,
                onCall = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${driver.phone}"))
                    context.startActivity(intent)
                }
            )
        }

        // 6. Recent Duty Logs Header
        if (dutyLogs.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT HUB DUTY LOGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMutedGray,
                        letterSpacing = 0.8.sp
                    )
                    TextButton(onClick = onNavigateToRegister) {
                        Text("Full Register ➔", color = PrimaryPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(dutyLogs.take(3), key = { it.id }) { log ->
                ManagerDutyLogRow(log = log)
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    subtext: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 11.sp, color = TextDark, fontWeight = FontWeight.Medium)
        Text(text = subtext, fontSize = 10.sp, color = TextMutedGray)
    }
}

@Composable
private fun ManagerDriverCard(
    driver: DriverEntity,
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
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurpleContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = driver.name.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PrimaryPurple
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(driver.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                Text(driver.phone, fontSize = 11.sp, color = TextGray)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ActiveGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("ASSIGNED", color = ActiveGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onCall,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ActiveGreenBg)
            ) {
                Icon(Icons.Default.Phone, contentDescription = "Call", tint = ActiveGreen, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun ManagerDutyLogRow(log: DutyLogEntity) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${log.driverName} • ${log.vehicleNo}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextDark
                )
                Text(
                    text = "${log.loginDate} • ${log.trips} Trips",
                    fontSize = 11.sp,
                    color = TextGray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${log.earnings.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ActiveGreen
                )
                Text(
                    text = "Comm: ₹${log.commissionRent.toInt()}",
                    fontSize = 11.sp,
                    color = PrimaryPurple
                )
            }
        }
    }
}

