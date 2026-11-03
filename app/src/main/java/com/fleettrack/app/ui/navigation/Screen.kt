package com.fleettrack.app.ui.navigation

sealed class Screen(val route: String) {
    // Splash & Intro
    data object IntroSplash : Screen("intro_splash")

    // Authentication & Entry
    data object Login : Screen("auth_login")

    // Mode selector
    data object ModeSelect : Screen("mode_select")

    // Driver Companion Routes
    data object DriverToday : Screen("driver_today")
    data object SetupWizard : Screen("driver_setup")
    data object PrivacyConsent : Screen("driver_privacy")

    // Fleet Owner Main Tabs (matching Fleet Manager Design)
    data object Dashboard : Screen("owner_dashboard")
    data object Cars : Screen("owner_cars")
    data object Trips : Screen("owner_trips")
    data object Earnings : Screen("owner_earnings")
    data object More : Screen("owner_more")

    // Sub-screens
    data object VehicleDetail : Screen("owner_vehicle_detail/{vehicleId}") {
        fun createRoute(vehicleId: Long) = "owner_vehicle_detail/$vehicleId"
    }
    data object DriverDetail : Screen("owner_driver_detail/{driverId}") {
        fun createRoute(driverId: Long) = "owner_driver_detail/$driverId"
    }
    data object Drivers : Screen("owner_drivers")
    data object Maintenance : Screen("owner_maintenance")
    data object Expenses : Screen("owner_expenses")
    data object Settings : Screen("owner_settings")
    data object ReviewQueue : Screen("owner_review_queue")
    data object Reports : Screen("owner_reports")
    data object Alerts : Screen("owner_alerts")

    // Backward compatibility aliases
    val FleetBoard get() = Dashboard
    val LiveFeed get() = Trips
}
