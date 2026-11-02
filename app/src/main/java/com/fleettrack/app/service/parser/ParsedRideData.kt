package com.fleettrack.app.service.parser

import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus

data class ParsedRideData(
    val platform: Platform,
    val status: RideStatus,
    val fare: Double? = null,
    val distanceKm: Double? = null,
    val durationMin: Double? = null,
    val pickup: String? = null,
    val dropoff: String? = null,
    val rideKey: String? = null,
    val confidence: Double = 1.0,
    val rawExtracted: String? = null
)

