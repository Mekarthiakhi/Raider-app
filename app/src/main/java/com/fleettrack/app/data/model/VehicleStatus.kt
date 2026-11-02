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
    val batteryLevel: Int?,
    val currentDriverName: String? = null,
    val currentDriverPhone: String? = null,
    val uberFare: Double = 0.0,
    val uberRides: Int = 0,
    val rapidoFare: Double = 0.0,
    val rapidoRides: Int = 0,
    val otherFare: Double = 0.0,
    val otherRides: Int = 0
)
