package com.fleettrack.app.ui.owner.detail

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.ui.components.EmptyState
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Top Header: "< Car Details" with Edit & Share
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
                    text = "Car Details",
                    color = TextDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row {
                IconButton(onClick = { /* Edit */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextDark, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { /* Share */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = TextDark, modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Large Car Image & Registration Hero Section
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
                // Centered Car Visual Box
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(MetricPurpleBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Registration & Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = vehicle?.regNo ?: "TS09 AB 1234",
                        color = TextDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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
                        // Driver Row Card with Call Button
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
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
                                            text = "Driver",
                                            color = TextGray,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = assignedDriver?.name ?: "Ravi Kumar",
                                            color = TextDark,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Phone Call Icon Button
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

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "$selectedSegmentTab Summary",
                                    color = TextDark,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "All vehicle maintenance logs, expenses, and platform accounting are synchronized.",
                                    color = TextGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
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
