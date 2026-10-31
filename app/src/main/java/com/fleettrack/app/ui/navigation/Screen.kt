package com.fleettrack.app.ui.navigation

sealed class Screen(val route: String) {
    // Mode selector
    data object ModeSelect : Screen("mode_select")

    // Driver Companion Routes
    data object DriverToday : Screen("driver_today")
    data object SetupWizard : Screen("driver_setup")
    data object PrivacyConsent : Screen("driver_privacy")

    // Fleet Owner Routes
    data object FleetBoard : Screen("owner_fleet_board")
    data object LiveFeed : Screen("owner_live_feed")
    data object VehicleDetail : Screen("owner_vehicle_detail/{vehicleId}") {
        fun createRoute(vehicleId: Long) = "owner_vehicle_detail/$vehicleId"
    }
    data object ReviewQueue : Screen("owner_review_queue")
    data object Reports : Screen("owner_reports")
    data object Alerts : Screen("owner_alerts")
}

