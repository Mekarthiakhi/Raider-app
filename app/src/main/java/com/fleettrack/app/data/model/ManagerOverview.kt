package com.fleettrack.app.data.model

data class ManagerOverview(
    val managerId: String,
    val managerName: String,
    val phone: String,
    val hubName: String,
    val totalDrivers: Int = 0,
    val activeDrivers: Int = 0,
    val totalVehicles: Int = 0,
    val activeVehicles: Int = 0,
    val todayEarnings: Double = 0.0,
    val todayTrips: Int = 0,
    val monthlyEarnings: Double = 0.0,
    val commissionGenerated: Double = 0.0,
    val isActive: Boolean = true
)

