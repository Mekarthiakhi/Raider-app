package com.fleettrack.app.data.model

data class DriverPerformance(
    val driverId: Long,
    val driverName: String,
    val phone: String,
    val assignedVehicleRegNo: String?,
    val assignedVehicleNickname: String?,
    val isShiftActive: Boolean,
    val todayFare: Double,
    val todayRides: Int,
    val todayKm: Double,
    val uberFare: Double = 0.0,
    val uberRides: Int = 0,
    val rapidoFare: Double = 0.0,
    val rapidoRides: Int = 0,
    val otherFare: Double = 0.0,
    val otherRides: Int = 0
)

