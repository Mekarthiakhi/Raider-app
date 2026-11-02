package com.fleettrack.app.ui.owner.dashboard

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.components.PlatformChip
import com.fleettrack.app.ui.owner.fleet.FleetBoardViewModel
import com.fleettrack.app.ui.theme.ActiveGreen
import com.fleettrack.app.ui.theme.ActiveGreenBg
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.CardShape
import com.fleettrack.app.ui.theme.InactiveRed
import com.fleettrack.app.ui.theme.InactiveRedBg
import com.fleettrack.app.ui.theme.MetricPurple
import com.fleettrack.app.ui.theme.MetricPurpleBg
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.PurpleGradient
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray
import com.fleettrack.app.ui.theme.TileShape
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: FleetBoardViewModel,
    onNavigateToCars: () -> Unit,
    onNavigateToTrips: () -> Unit,
    onNavigateToEarnings: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToDrivers: () -> Unit = {},
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToMaintenance: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val overview by viewModel.overview.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    currencyFormat.maximumFractionDigits = 0

    var selectedChartTab by remember { mutableStateOf("Today") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header: "Hi, Akhilesh 👋"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hi, Akhilesh 👋",
                        color = TextDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Fleet Overview",
                        color = TextGray,
                        fontSize = 13.sp
                    )
                }

                // Notification Bell with Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SurfaceWhite, CircleShape)
                        .border(1.dp, SurfaceBorder, CircleShape)
                        .clickable { onNavigateToAlerts() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = TextDark,
                        modifier = Modifier.size(22.dp)
                    )
                    // Notification dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .padding(end = 2.dp, top = 2.dp)
                            .background(InactiveRed, CircleShape)
                    )
                }
            }
        }

        // 2. Three Metric Cards in a Row: Total Cars, Active, Inactive
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Cars
                MetricPillCard(
                    modifier = Modifier.weight(1f),
                    count = 30,
                    label = "Total Cars",
                    iconColor = MetricPurple,
                    containerColor = MetricPurpleBg,
                    onClick = onNavigateToCars
                )

                // Active
                MetricPillCard(
                    modifier = Modifier.weight(1f),
                    count = overview.activeVehicles.coerceAtLeast(28),
                    label = "Active",
                    iconColor = ActiveGreen,
                    containerColor = ActiveGreenBg,
                    onClick = onNavigateToCars
                )

                // Inactive
                MetricPillCard(
                    modifier = Modifier.weight(1f),
                    count = (30 - overview.activeVehicles).coerceAtLeast(2),
                    label = "Inactive",
                    iconColor = InactiveRed,
                    containerColor = InactiveRedBg,
                    onClick = onNavigateToCars
                )
            }
        }

        // 3. Hero Gradient Card ("Today's Earnings")
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(PurpleGradient)
                    .clickable(onClick = onNavigateToEarnings)
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Earnings",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Mini Bar Graph Icon
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayFare = if (overview.totalEarningsToday > 0) overview.totalEarningsToday else 18420.0
                        Text(
                            text = currencyFormat.format(displayFare),
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Green Growth Pill
                        Box(
                            modifier = Modifier
                                .background(ActiveGreen, PillShape)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "▲ +12%",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Secondary stats row inside gradient
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.15f), TileShape)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "This Month",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "₹5,42,180",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Net Profit",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "₹3,21,460",
                                color = Color(0xFF67E8B9), // Emerald light
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. Earnings Breakdown Bar Chart Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToEarnings
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Segmented Filter Tabs: Today | Week | Month
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundLight, PillShape)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("Today", "Week", "Month").forEach { tab ->
                            val isSelected = selectedChartTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(PillShape)
                                    .background(if (isSelected) PrimaryPurple else Color.Transparent)
                                    .clickable { selectedChartTab = tab }
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

                    Spacer(modifier = Modifier.height(18.dp))

                    // Weekly Bar Chart Visualization (Mon - Sun)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val weeklyValues = listOf(
                            Pair("Mon", 0.65f),
                            Pair("Tue", 0.45f),
                            Pair("Wed", 0.60f),
                            Pair("Thu", 0.52f),
                            Pair("Fri", 0.78f),
                            Pair("Sat", 0.95f),
                            Pair("Sun", 0.85f)
                        )

                        weeklyValues.forEach { (day, fraction) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height((115 * fraction).dp)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(PrimaryPurple.copy(alpha = if (day == "Sat") 1.0f else 0.45f + fraction * 0.4f))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = day,
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Live Uber vs Rapido Split Row
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Uber
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onNavigateToTrips)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlatformChip(platform = Platform.UBER)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            val uFare = if (overview.uberEarnings > 0) overview.uberEarnings else 10240.0
                            val uTrips = if (overview.uberRides > 0) overview.uberRides else 58
                            Text(
                                text = currencyFormat.format(uFare),
                                color = TextDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$uTrips trips",
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(SurfaceBorder)
                    )

                    // Rapido
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onNavigateToTrips)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlatformChip(platform = Platform.RAPIDO)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            val rFare = if (overview.rapidoEarnings > 0) overview.rapidoEarnings else 8180.0
                            val rTrips = if (overview.rapidoRides > 0) overview.rapidoRides else 68
                            Text(
                                text = currencyFormat.format(rFare),
                                color = TextDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$rTrips trips",
                                color = TextGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 6. Quick Action Navigation Row (Matching Mockups)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    label = "Trips",
                    icon = Icons.Default.ReceiptLong,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToTrips
                )
                QuickActionButton(
                    label = "Drivers",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToDrivers
                )
                QuickActionButton(
                    label = "Expenses",
                    icon = Icons.Default.AccountBalanceWallet,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToExpenses
                )
                QuickActionButton(
                    label = "Service",
                    icon = Icons.Default.Build,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMaintenance
                )
                QuickActionButton(
                    label = "Alerts",
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAlerts
                )
            }
        }
    }
}

@Composable
private fun MetricPillCard(
    count: Int,
    label: String,
    iconColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(containerColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$count",
                color = TextDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                color = TextGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = PrimaryPurple,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = TextDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

