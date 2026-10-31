package com.fleettrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.fleettrack.app.ui.navigation.FleetNavigation
import com.fleettrack.app.ui.theme.FleetTrackTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FleetTrackTheme {
                val navController = rememberNavController()
                FleetNavigation(navController = navController)
            }
        }
    }
}

