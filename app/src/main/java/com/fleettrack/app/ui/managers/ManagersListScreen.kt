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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.fleettrack.app.data.local.entity.ManagerEntity
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
fun ManagersListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToManagerDetail: (String) -> Unit,
    viewModel: ManagersViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val managers by viewModel.managers.collectAsState()
    val unassignedDrivers by viewModel.unassignedDrivers.collectAsState()
    val unassignedVehicles by viewModel.unassignedVehicles.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundLight,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryPurple,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Manager")
            }
        }
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
                            text = "Hub Fleet Managers",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "3-Tier Hierarchy: Owner ➔ Managers ➔ Drivers",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                }
            }

            // Summary Metrics Card
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
                                text = "${managers.size}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple
                            )
                            Text(
                                text = "Hub Managers",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(SurfaceBorder)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${30 - unassignedDrivers.size}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = ActiveGreen
                            )
                            Text(
                                text = "Drivers Assigned",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(SurfaceBorder)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${30 - unassignedVehicles.size}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                            Text(
                                text = "Cabs Assigned",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                    }
                }
            }

            // Unassigned Alert Banner (if any unassigned cars/drivers exist)
            if (unassignedDrivers.isNotEmpty() || unassignedVehicles.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDBA74)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Unassigned Fleet Assets",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF9A3412)
                                )
                                Text(
                                    text = "${unassignedDrivers.size} drivers and ${unassignedVehicles.size} vehicles are currently unassigned to any hub.",
                                    fontSize = 11.sp,
                                    color = TextDark
                                )
                            }
                        }
                    }
                }
            }

            // Managers List Header
            item {
                Text(
                    text = "ACTIVE HUB MANAGERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMutedGray,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            // List of Managers
            items(managers, key = { it.id }) { manager ->
                ManagerCard(
                    manager = manager,
                    onManageClick = { onNavigateToManagerDetail(manager.id) },
                    onCallClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${manager.phone}"))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddEditManagerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, hubName, pinCode ->
                viewModel.addManager(name, phone, hubName, pinCode)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ManagerCard(
    manager: ManagerEntity,
    onManageClick: () -> Unit,
    onCallClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onManageClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hub Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryPurpleContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "📍 ${manager.hubName}",
                        color = PrimaryPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Active Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ActiveGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (manager.isActive) "ACTIVE" else "INACTIVE",
                        color = if (manager.isActive) ActiveGreen else Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Manager Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = manager.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PrimaryPurple
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = manager.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "+91 ${manager.phone}",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }

                // Call Button
                IconButton(
                    onClick = onCallClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9))
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call",
                        tint = ActiveGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer with PIN & Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF8F9FA))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TextMutedGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PIN: ${manager.pinCode}",
                        fontSize = 11.sp,
                        color = TextMutedGray,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onManageClick)
                ) {
                    Text(
                        text = "Manage Team & Cabs",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

