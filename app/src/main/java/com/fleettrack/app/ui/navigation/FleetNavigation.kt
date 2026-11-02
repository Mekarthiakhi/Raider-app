package com.fleettrack.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.driver.privacy.PrivacyScreen
import com.fleettrack.app.ui.driver.setup.SetupWizardScreen
import com.fleettrack.app.ui.driver.setup.SetupWizardViewModel
import com.fleettrack.app.ui.driver.today.DriverTodayScreen
import com.fleettrack.app.ui.driver.today.DriverTodayViewModel
import com.fleettrack.app.ui.owner.alerts.AlertsScreen
import com.fleettrack.app.ui.owner.alerts.AlertsViewModel
import com.fleettrack.app.ui.owner.cars.CarsListScreen
import com.fleettrack.app.ui.owner.dashboard.DashboardScreen
import com.fleettrack.app.ui.owner.detail.VehicleDetailScreen
import com.fleettrack.app.ui.owner.detail.VehicleDetailViewModel
import com.fleettrack.app.ui.owner.drivers.DriversScreen
import com.fleettrack.app.ui.owner.expenses.ExpensesScreen
import com.fleettrack.app.ui.owner.feed.LiveFeedScreen
import com.fleettrack.app.ui.owner.feed.LiveFeedViewModel
import com.fleettrack.app.ui.owner.fleet.FleetBoardViewModel
import com.fleettrack.app.ui.owner.maintenance.MaintenanceScreen
import com.fleettrack.app.ui.owner.more.MoreScreen
import com.fleettrack.app.ui.owner.reports.ReportsScreen
import com.fleettrack.app.ui.owner.reports.ReportsViewModel
import com.fleettrack.app.ui.owner.review.ReviewQueueScreen
import com.fleettrack.app.ui.owner.review.ReviewQueueViewModel
import com.fleettrack.app.ui.owner.settings.SettingsScreen
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.CardShape
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.PurpleGradient
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray

@Composable
fun FleetNavigation(
    navController: NavHostController,
    startDestination: String = Screen.ModeSelect.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isOwnerRoute = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Cars.route,
        Screen.Trips.route,
        Screen.Earnings.route,
        Screen.More.route
    )

    val isDriverRoute = currentRoute in listOf(
        Screen.DriverToday.route,
        Screen.SetupWizard.route,
        Screen.PrivacyConsent.route
    )

    Scaffold(
        bottomBar = {
            if (isOwnerRoute) {
                OwnerBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            } else if (isDriverRoute) {
                DriverBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(Screen.DriverToday.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ==========================================
            // Welcome & Mode Selection Entry
            // ==========================================
            composable(Screen.ModeSelect.route) {
                ModeSelectionScreen(
                    onSelectDriver = { navController.navigate(Screen.SetupWizard.route) },
                    onSelectOwner = { navController.navigate(Screen.Dashboard.route) }
                )
            }

            // ==========================================
            // Driver Flow
            // ==========================================
            composable(Screen.SetupWizard.route) {
                val vm: SetupWizardViewModel = hiltViewModel()
                SetupWizardScreen(
                    viewModel = vm,
                    onSetupComplete = {
                        navController.navigate(Screen.DriverToday.route) {
                            popUpTo(Screen.ModeSelect.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.DriverToday.route) {
                val vm: DriverTodayViewModel = hiltViewModel()
                DriverTodayScreen(viewModel = vm)
            }

            composable(Screen.PrivacyConsent.route) {
                PrivacyScreen()
            }

            // ==========================================
            // Fleet Owner Flow (Fleet Manager 5 Tabs)
            // ==========================================
            composable(Screen.Dashboard.route) {
                val vm: FleetBoardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = vm,
                    onNavigateToCars = { navController.navigate(Screen.Cars.route) },
                    onNavigateToTrips = { navController.navigate(Screen.Trips.route) },
                    onNavigateToEarnings = { navController.navigate(Screen.Earnings.route) },
                    onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) }
                )
            }

            composable(Screen.Cars.route) {
                val vm: FleetBoardViewModel = hiltViewModel()
                CarsListScreen(
                    viewModel = vm,
                    onVehicleClick = { vehicleId ->
                        navController.navigate(Screen.VehicleDetail.createRoute(vehicleId))
                    }
                )
            }

            composable(Screen.Trips.route) {
                val vm: LiveFeedViewModel = hiltViewModel()
                LiveFeedScreen(
                    viewModel = vm,
                    onRideClick = { rideId -> }
                )
            }

            composable(Screen.Earnings.route) {
                val vm: ReportsViewModel = hiltViewModel()
                ReportsScreen(
                    viewModel = vm,
                    onExportCsv = { },
                    onExportPdf = { }
                )
            }

            composable(Screen.More.route) {
                MoreScreen(
                    onNavigateToReports = { navController.navigate(Screen.Earnings.route) },
                    onNavigateToMaintenance = { navController.navigate(Screen.Maintenance.route) },
                    onNavigateToDrivers = { navController.navigate(Screen.Drivers.route) },
                    onNavigateToExpenses = { navController.navigate(Screen.Expenses.route) },
                    onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            // ==========================================
            // Fleet Owner Sub-Screens
            // ==========================================
            composable(Screen.VehicleDetail.route) {
                val vm: VehicleDetailViewModel = hiltViewModel()
                VehicleDetailScreen(
                    viewModel = vm,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Drivers.route) {
                val vm: FleetBoardViewModel = hiltViewModel()
                DriversScreen(
                    viewModel = vm,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Maintenance.route) {
                MaintenanceScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Expenses.route) {
                ExpensesScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = {
                        navController.navigate(Screen.ModeSelect.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Alerts.route) {
                val vm: AlertsViewModel = hiltViewModel()
                AlertsScreen(
                    viewModel = vm,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.ReviewQueue.route) {
                val vm: ReviewQueueViewModel = hiltViewModel()
                ReviewQueueScreen(
                    viewModel = vm,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Reports.route) {
                val vm: ReportsViewModel = hiltViewModel()
                ReportsScreen(
                    viewModel = vm,
                    onExportCsv = { },
                    onExportPdf = { },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun ModeSelectionScreen(
    onSelectDriver: () -> Unit,
    onSelectOwner: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // App Logo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurpleContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "Fleet Manager",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Fleet Manager",
                color = TextDark,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Manage your Uber & Rapido cars, trips, and earnings in one place.",
                color = TextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Fleet Owner Button (Primary Action)
            Button(
                onClick = onSelectOwner,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = PillShape
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SupervisorAccount,
                        contentDescription = "Owner",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Fleet Owner (Akhilesh)",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Driver Companion Button (Secondary Action)
            GlassCard(
                onClick = onSelectDriver,
                shape = PillShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Driver",
                        tint = PrimaryPurple
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Driver Telemetry Companion",
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
