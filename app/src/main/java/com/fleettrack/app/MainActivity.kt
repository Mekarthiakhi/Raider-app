package com.fleettrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.fleettrack.app.data.local.FleetDataSeeder
import com.fleettrack.app.data.repository.DriverRepository
import com.fleettrack.app.data.repository.DutyLogRepository
import com.fleettrack.app.data.repository.RideRepository
import com.fleettrack.app.data.repository.VehicleRepository
import com.fleettrack.app.ui.navigation.FleetNavigation
import com.fleettrack.app.ui.theme.FleetTrackTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var seeder: FleetDataSeeder

    @Inject
    lateinit var vehicleRepository: VehicleRepository

    @Inject
    lateinit var driverRepository: DriverRepository

    @Inject
    lateinit var dutyLogRepository: DutyLogRepository

    @Inject
    lateinit var rideRepository: RideRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Seed 30 cabs, drivers, and Uber+Rapido rides if database is empty & sync with Supabase
        lifecycleScope.launch(Dispatchers.IO) {
            seeder.seedIfEmpty()
            try {
                vehicleRepository.syncVehiclesFromSupabase()
                driverRepository.syncDriversFromSupabase()
                dutyLogRepository.syncFromSupabase()
                rideRepository.syncPendingRides()
                Timber.d("MainActivity: Cloud sync with Supabase completed successfully")
            } catch (e: Exception) {
                Timber.w(e, "MainActivity: Supabase sync fallback")
            }
        }

        setContent {
            FleetTrackTheme {
                val navController = rememberNavController()
                val sessionPrefs = getSharedPreferences("svm_fleet_session", android.content.Context.MODE_PRIVATE)
                val isLoggedIn = sessionPrefs.getBoolean("is_logged_in", false)
                val role = sessionPrefs.getString("role", null)
                val isShiftActive = sessionPrefs.getBoolean("is_shift_active", false)

                val startScreen = when {
                    isLoggedIn && role == "OWNER" -> com.fleettrack.app.ui.navigation.Screen.Dashboard.route
                    isLoggedIn && role == "DRIVER" -> com.fleettrack.app.ui.navigation.Screen.DriverToday.route
                    else -> com.fleettrack.app.ui.navigation.Screen.IntroSplash.route
                }

                FleetNavigation(
                    navController = navController,
                    startDestination = startScreen
                )
            }
        }
    }
}
