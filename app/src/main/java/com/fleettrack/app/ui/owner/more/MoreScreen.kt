package com.fleettrack.app.ui.owner.more

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.PurpleGradient
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray
import com.fleettrack.app.ui.theme.TileShape

@Composable
fun MoreScreen(
    onNavigateToReports: () -> Unit,
    onNavigateToMaintenance: () -> Unit,
    onNavigateToDrivers: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLiveTracking: () -> Unit = {},
    onNavigateToDocumentVault: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Text(
                text = "More",
                color = TextDark,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 2. Profile Card: "Akhilesh Mekarthi - Fleet Owner"
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToSettings,
                shape = TileShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(PurpleGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AM",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Akhilesh Mekarthi",
                                color = TextDark,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Fleet Owner",
                                color = TextGray,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Profile Details",
                        tint = TextMutedGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 3. Section Title
        item {
            Text(
                text = "Fleet Operations",
                color = TextGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        // 4. Menu Items
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Live Tracking (Sheet 2 Feature Suggestion & Screen)
                MoreMenuItem(
                    icon = Icons.Default.Navigation,
                    title = "Live Tracking",
                    subtitle = "Real-time location & trip status on map",
                    onClick = onNavigateToLiveTracking
                )

                MoreMenuItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    title = "Reports & Analytics",
                    subtitle = "Car-wise & overall earnings, net profit",
                    onClick = onNavigateToReports
                )

                MoreMenuItem(
                    icon = Icons.Default.Build,
                    title = "Maintenance & Service",
                    subtitle = "Service, insurance, PUC expiry alerts",
                    onClick = onNavigateToMaintenance
                )

                MoreMenuItem(
                    icon = Icons.Default.People,
                    title = "Drivers",
                    subtitle = "28 Active drivers, shifts & payouts",
                    onClick = onNavigateToDrivers
                )

                MoreMenuItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Expenses",
                    subtitle = "Fuel, maintenance, driver payments",
                    onClick = onNavigateToExpenses
                )

                // Document Vault (Sheet 2 Feature Suggestion)
                MoreMenuItem(
                    icon = Icons.Default.Description,
                    title = "Document Vault",
                    subtitle = "RC, Insurance, PUC & Permits",
                    onClick = onNavigateToDocumentVault
                )

                MoreMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Alerts & Reminders",
                    subtitle = "Renewal notices, idle alerts, fastag",
                    onClick = onNavigateToAlerts
                )

                MoreMenuItem(
                    icon = Icons.Default.Settings,
                    title = "Settings",
                    subtitle = "Team roles, Uber integration, theme",
                    onClick = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PrimaryPurpleContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        color = TextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = TextMutedGray,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open $title",
                tint = TextMutedGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
