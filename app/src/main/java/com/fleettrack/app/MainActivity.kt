package com.fleettrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.fleettrack.app.data.local.FleetDataSeeder
import com.fleettrack.app.ui.navigation.FleetNavigation
import com.fleettrack.app.ui.theme.FleetTrackTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var seeder: FleetDataSeeder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Seed 30 cabs, drivers, and Uber+Rapido rides if database is empty
        lifecycleScope.launch(Dispatchers.IO) {
            seeder.seedIfEmpty()
        }

        setContent {
            FleetTrackTheme {
                val navController = rememberNavController()
                FleetNavigation(navController = navController)
            }
        }
    }
}
