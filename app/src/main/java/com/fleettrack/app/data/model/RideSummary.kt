package com.fleettrack.app.data.model

data class RideSummary(
    val rideCount: Int = 0,
    val totalFare: Double = 0.0,
    val totalKm: Double = 0.0,
    val avgFarePerKm: Double = if (totalKm > 0) totalFare / totalKm else 0.0,
    val totalDuration: Int = 0,
    val cancelledCount: Int = 0
)
