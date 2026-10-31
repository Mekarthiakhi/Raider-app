package com.fleettrack.app.data.model

import kotlinx.datetime.Instant

data class VehicleStatus(
    val vehicleId: Long,
    val regNo: String,
    val nickname: String?,
    val isOnline: Boolean,
    val currentRideStatus: RideStatus,
    val todayFare: Double,
    val todayRides: Int,
    val todayKm: Double,
    val lastSeen: Instant?,
    val batteryLevel: Int?
)
