package com.fleettrack.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.fleettrack.app.ui.owner.detail.VehicleDetailScreen
import com.fleettrack.app.ui.owner.detail.VehicleDetailViewModel
import com.fleettrack.app.ui.owner.feed.LiveFeedScreen
import com.fleettrack.app.ui.owner.feed.LiveFeedViewModel
import com.fleettrack.app.ui.owner.fleet.FleetBoardScreen
import com.fleettrack.app.ui.owner.fleet.FleetBoardViewModel
import com.fleettrack.app.ui.owner.reports.ReportsScreen
import com.fleettrack.app.ui.owner.reports.ReportsViewModel
import com.fleettrack.app.ui.owner.review.ReviewQueueScreen
import com.fleettrack.app.ui.owner.review.ReviewQueueViewModel
import com.fleettrack.app.ui.theme.BackgroundDark
import com.fleettrack.app.ui.theme.EmeraldAccent
import com.fleettrack.app.ui.theme.SurfaceBorderDark
import com.fleettrack.app.ui.theme.TextMuted
import com.fleettrack.app.ui.theme.TextPrimary
import com.fleettrack.app.ui.theme.TextSecondary
import com.fleettrack.app.ui.theme.TileShape

@Composable
fun FleetNavigation(
    navController: NavHostController,
    startDestination: String = Screen.ModeSelect.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isOwnerRoute = currentRoute in listOf(
        Screen.FleetBoard.route,
        Screen.LiveFeed.route,
        Screen.Reports.route,
        Screen.Alerts.route
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
                            popUpTo(Screen.FleetBoard.route) { saveState = true }
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
            // Mode Selection Entry
            composable(Screen.ModeSelect.route) {
                ModeSelectionScreen(
                    onSelectDriver = { navController.navigate(Screen.SetupWizard.route) },
                    onSelectOwner = { navController.navigate(Screen.FleetBoard.route) }
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
            // Fleet Owner Flow
            // ==========================================
            composable(Screen.FleetBoard.route) {
                val vm: FleetBoardViewModel = hiltViewModel()
                FleetBoardScreen(
                    viewModel = vm,
                    onVehicleClick = { vehicleId ->
                        navController.navigate(Screen.VehicleDetail.createRoute(vehicleId))
                    },
                    onReviewQueueClick = {
                        navController.navigate(Screen.ReviewQueue.route)
                    }
                )
            }

            composable(Screen.LiveFeed.route) {
                val vm: LiveFeedViewModel = hiltViewModel()
                LiveFeedScreen(
                    viewModel = vm,
                    onRideClick = { rideId ->
                        // Navigate to review or detail
                    }
                )
            }

            composable(Screen.VehicleDetail.route) {
                val vm: VehicleDetailViewModel = hiltViewModel()
                VehicleDetailScreen(
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
                    onExportCsv = { /* trigger CSV export */ },
                    onExportPdf = { /* trigger PDF export */ }
                )
            }

            composable(Screen.Alerts.route) {
                val vm: AlertsViewModel = hiltViewModel()
                AlertsScreen(viewModel = vm)
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
            .background(BackgroundDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "FLEET TRACK",
                color = EmeraldAccent,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Autonomous Ride & Fare Telemetry",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Driver Companion Card
            GlassCard(
                onClick = onSelectDriver,
                shape = TileShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = EmeraldAccent,
                        modifier = Modifier.height(36.dp).width(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Driver Car Phone",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Runs silently in background. Reads Rapido, Uber, Ola fares on trip completion.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fleet Captain / Owner Card
            GlassCard(
                onClick = onSelectOwner,
                shape = TileShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SupervisorAccount,
                        contentDescription = null,
                        tint = EmeraldAccent,
                        modifier = Modifier.height(36.dp).width(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Fleet Captain / Owner",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Live 30-car fleet board, earnings stream, review queue, and accounting reports.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

