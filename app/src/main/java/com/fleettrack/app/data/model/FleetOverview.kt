package com.fleettrack.app.data.model

data class FleetOverview(
    val totalEarningsToday: Double = 0.0,
    val totalRidesToday: Int = 0,
    val totalKmToday: Double = 0.0,
    val activeVehicles: Int = 0,
    val offlineVehicles: Int = 0,
    val onTripVehicles: Int = 0
)
