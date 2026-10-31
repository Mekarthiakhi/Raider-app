package com.fleettrack.app.data.remote

import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.local.entity.RideEventEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RideDto(
    @SerialName("vehicle_id") val vehicleId: String,
    @SerialName("driver_id") val driverId: String,
    @SerialName("device_id") val deviceId: String? = null,
    @SerialName("platform") val platform: String,
    @SerialName("ride_key") val rideKey: String,
    @SerialName("status") val status: String,
    @SerialName("accepted_at") val acceptedAt: String,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("fare") val fare: Double? = null,
    @SerialName("distance_km") val distanceKm: Double? = null,
    @SerialName("duration_min") val durationMin: Double? = null,
    @SerialName("pickup") val pickup: String? = null,
    @SerialName("dropoff") val dropoff: String? = null,
    @SerialName("confidence") val confidence: Double? = null,
    @SerialName("needs_review") val needsReview: Boolean = false,
    @SerialName("review_note") val reviewNote: String? = null,
    @SerialName("raw_extracted") val rawExtracted: String? = null
)

@Serializable
data class RideEventDto(
    @SerialName("ride_id") val rideId: String,
    @SerialName("event_type") val eventType: String,
    @SerialName("occurred_at") val occurredAt: String,
    @SerialName("raw_json") val rawJson: String? = null
)

interface RideRemoteDataSource {
    suspend fun uploadRide(ride: RideEntity): Result<String>
    suspend fun uploadRideEvent(event: RideEventEntity): Result<Unit>
    suspend fun syncPendingRides(rides: List<RideEntity>): Result<Int>
}

class RideRemoteDataSourceImpl : RideRemoteDataSource {
    override suspend fun uploadRide(ride: RideEntity): Result<String> {
        // Will be connected to Supabase Postgrest in Stage 4
        return Result.success("synced_${ride.rideKey}")
    }

    override suspend fun uploadRideEvent(event: RideEventEntity): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun syncPendingRides(rides: List<RideEntity>): Result<Int> {
        return Result.success(rides.size)
    }
}

