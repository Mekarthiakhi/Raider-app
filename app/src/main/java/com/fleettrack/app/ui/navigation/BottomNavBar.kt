package com.fleettrack.app.ui.navigation

import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextMutedGray

sealed class NavItem(val screen: Screen, val label: String, val icon: ImageVector) {
    // Owner 5 Tabs (Fleet Manager Design)
    data object Home : NavItem(Screen.Dashboard, "Home", Icons.Default.Home)
    data object Cars : NavItem(Screen.Cars, "Cars", Icons.Default.DirectionsCar)
    data object Trips : NavItem(Screen.Trips, "Trips", Icons.Default.ReceiptLong)
    data object Earnings : NavItem(Screen.Earnings, "Earnings", Icons.Default.AccountBalanceWallet)
    data object More : NavItem(Screen.More, "More", Icons.Default.Menu)

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
    val items = listOf(
        NavItem.Home,
        NavItem.Cars,
        NavItem.Trips,
        NavItem.Earnings,
        NavItem.More
    )

    NavigationBar(
        containerColor = SurfaceWhite,
        tonalElevation = 4.dp,
        modifier = Modifier.border(width = 1.dp, color = SurfaceBorder)
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) PrimaryPurple else TextMutedGray
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (isSelected) PrimaryPurple else TextMutedGray,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryPurple,
                    selectedTextColor = PrimaryPurple,
                    unselectedIconColor = TextMutedGray,
                    unselectedTextColor = TextMutedGray,
                    indicatorColor = PrimaryPurpleContainer
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
        containerColor = SurfaceWhite,
        tonalElevation = 4.dp,
        modifier = Modifier.border(width = 1.dp, color = SurfaceBorder)
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) PrimaryPurple else TextMutedGray
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (isSelected) PrimaryPurple else TextMutedGray,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryPurple,
                    selectedTextColor = PrimaryPurple,
                    unselectedIconColor = TextMutedGray,
                    unselectedTextColor = TextMutedGray,
                    indicatorColor = PrimaryPurpleContainer
                )
            )
        }
    }
}
