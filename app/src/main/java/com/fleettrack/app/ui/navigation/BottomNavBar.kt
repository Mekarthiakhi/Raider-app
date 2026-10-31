package com.fleettrack.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fleettrack.app.ui.theme.BackgroundDark
import com.fleettrack.app.ui.theme.EmeraldAccent
import com.fleettrack.app.ui.theme.SurfaceBorderDark
import com.fleettrack.app.ui.theme.SurfaceDark
import com.fleettrack.app.ui.theme.TextMuted
import com.fleettrack.app.ui.theme.TextPrimary

sealed class NavItem(val screen: Screen, val label: String, val icon: ImageVector) {
    // Owner Tabs
    data object Fleet : NavItem(Screen.FleetBoard, "Fleet", Icons.Default.DirectionsCar)
    data object Feed : NavItem(Screen.LiveFeed, "Live Feed", Icons.Default.DynamicFeed)
    data object Reports : NavItem(Screen.Reports, "Reports", Icons.Default.Assessment)
    data object Alerts : NavItem(Screen.Alerts, "Alerts", Icons.Default.Notifications)

    // Driver Tabs
    data object Today : NavItem(Screen.DriverToday, "Today", Icons.Default.Today)
    data object Setup : NavItem(Screen.SetupWizard, "Setup", Icons.Default.Settings)
    data object Privacy : NavItem(Screen.PrivacyConsent, "Consent", Icons.Default.PrivacyTip)
}

@Composable
fun OwnerBottomBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    val items = listOf(NavItem.Fleet, NavItem.Feed, NavItem.Reports, NavItem.Alerts)

    NavigationBar(
        containerColor = SurfaceDark,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = EmeraldAccent,
                    selectedTextColor = EmeraldAccent,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = SurfaceBorderDark
                )
            )
        }
    }
}

@Composable
fun DriverBottomBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    val items = listOf(NavItem.Today, NavItem.Setup, NavItem.Privacy)

    NavigationBar(
        containerColor = SurfaceDark,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = EmeraldAccent,
                    selectedTextColor = EmeraldAccent,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = SurfaceBorderDark
                )
            )
        }
    }
}

