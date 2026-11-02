package com.fleettrack.app.ui.owner.alerts

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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Security
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

data class NotificationItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val timeAgo: String,
    val category: String,
    val icon: ImageVector,
    val iconColor: Color,
    val iconBg: Color
)

@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val unreadAlerts by viewModel.unreadAlerts.collectAsState()
    var selectedTab by remember { mutableStateOf("All") }

    val notifications = remember {
        listOf(
            NotificationItem(
                id = "1",
                title = "Service Due",
                subtitle = "TS09 AB 1234 • in 3 days",
                timeAgo = "Now",
                category = "Service",
                icon = Icons.Default.Build,
                iconColor = PrimaryPurple,
                iconBg = PrimaryPurpleContainer
            ),
            NotificationItem(
                id = "2",
                title = "Insurance Expiry",
                subtitle = "TS09 EF 9012 • in 18 days",
                timeAgo = "2h ago",
                category = "Expiry",
                icon = Icons.Default.Security,
                iconColor = ActiveGreen,
                iconBg = ActiveGreenBg
            ),
            NotificationItem(
                id = "3",
                title = "High Fuel Expense",
                subtitle = "TS09 CD 5678 • ₹5,400 (Today)",
                timeAgo = "4h ago",
                category = "Activity",
                icon = Icons.Default.LocalGasStation,
                iconColor = InactiveRed,
                iconBg = InactiveRedBg
            ),
            NotificationItem(
                id = "4",
                title = "Earnings Update",
                subtitle = "Today's earnings: ₹18,420",
                timeAgo = "6h ago",
                category = "Activity",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                iconColor = PrimaryPurple,
                iconBg = MetricPurpleBg
            ),
            NotificationItem(
                id = "5",
                title = "PUC Expiry Alert",
                subtitle = "TS09 GH 3456 • in 25 days",
                timeAgo = "1d ago",
                category = "Expiry",
                icon = Icons.Default.Warning,
                iconColor = InactiveRed,
                iconBg = InactiveRedBg
            )
        )
    }

    val filteredNotifications = if (selectedTab == "All") {
        notifications
    } else {
        notifications.filter { it.category.equals(selectedTab, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Top Header: "< Notifications"
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
                text = "Notifications",
                color = TextDark,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Filter Pills: All | Service | Expiry | Activity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NotificationFilterPill(
                label = "All",
                isSelected = selectedTab == "All",
                onClick = { selectedTab = "All" }
            )
            NotificationFilterPill(
                label = "Service",
                isSelected = selectedTab == "Service",
                onClick = { selectedTab = "Service" }
            )
            NotificationFilterPill(
                label = "Expiry",
                isSelected = selectedTab == "Expiry",
                onClick = { selectedTab = "Expiry" }
            )
            NotificationFilterPill(
                label = "Activity",
                isSelected = selectedTab == "Activity",
                onClick = { selectedTab = "Activity" }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Notification List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredNotifications, key = { it.id }) { item ->
                NotificationCardItem(item = item)
            }
        }
    }
}

@Composable
private fun NotificationFilterPill(
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
private fun NotificationCardItem(item: NotificationItem) {
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
                        .background(item.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = item.iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = item.title,
                        color = TextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.subtitle,
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = item.timeAgo,
                color = TextMutedGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
