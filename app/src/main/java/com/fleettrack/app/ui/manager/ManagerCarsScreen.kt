package com.fleettrack.app.ui.manager

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fleettrack.app.data.local.entity.VehicleEntity
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.theme.ActiveGreen
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray

@Composable
fun ManagerCarsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManagerDashboardViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val session by viewModel.managerSession.collectAsState()
    val vehicles by viewModel.assignedVehicles.collectAsState()

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
                            text = "Assigned Hub Cabs (${vehicles.size})",
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

            // Summary Card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${vehicles.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                            Text("Total Cabs", fontSize = 11.sp, color = TextGray)
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(SurfaceBorder))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${vehicles.count { it.status == "active" }}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ActiveGreen)
                            Text("On Duty Active", fontSize = 11.sp, color = TextGray)
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(SurfaceBorder))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${vehicles.count { it.status != "active" }}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEAB308))
                            Text("Idle / Standby", fontSize = 11.sp, color = TextGray)
                        }
                    }
                }
            }

            // List of Cabs
            items(vehicles, key = { it.id }) { vehicle ->
                VehicleCard(vehicle = vehicle)
            }
        }
    }
}

@Composable
private fun VehicleCard(vehicle: VehicleEntity) {
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
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.regNo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextDark
                )
                Text(
                    text = vehicle.nickname ?: "Commercial Cab",
                    fontSize = 12.sp,
                    color = TextGray
                )
                Text(
                    text = "SVM Commercial Fleet • Hyderabad",
                    fontSize = 10.sp,
                    color = TextMutedGray
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (vehicle.status == "active") ActiveGreen.copy(alpha = 0.15f) else Color(0xFFF3F4F6))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = vehicle.status.uppercase(),
                    color = if (vehicle.status == "active") ActiveGreen else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

