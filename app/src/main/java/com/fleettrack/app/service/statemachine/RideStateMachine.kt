package com.fleettrack.app.service.statemachine

import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.local.entity.RideEventEntity
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import com.fleettrack.app.data.model.SyncStatus
import com.fleettrack.app.data.repository.RideRepository
import com.fleettrack.app.service.parser.ParsedRideData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes

data class ActiveTripState(
    val localRideId: Long,
    val rideKey: String,
    val platform: Platform,
    val status: RideStatus,
    val acceptedAt: Instant,
    val vehicleId: Long,
    val driverId: Long,
    val pickup: String? = null,
    val lastUpdated: Instant = Clock.System.now()
)

data class TelemetryLogEntry(
    val timestamp: Instant = Clock.System.now(),
    val platform: Platform?,
    val eventType: String,
    val detectedStatus: RideStatus?,
    val fare: Double? = null,
    val distanceKm: Double? = null,
    val snippet: String
)

@Singleton
class RideStateMachine @Inject constructor(
    private val rideRepository: RideRepository
) {
    private val _activeTrip = MutableStateFlow<ActiveTripState?>(null)
    val activeTrip: StateFlow<ActiveTripState?> = _activeTrip.asStateFlow()

    private val _telemetryLogs = MutableStateFlow<List<TelemetryLogEntry>>(emptyList())
    val telemetryLogs: StateFlow<List<TelemetryLogEntry>> = _telemetryLogs.asStateFlow()

    fun logTelemetry(entry: TelemetryLogEntry) {
        val current = _telemetryLogs.value.toMutableList()
        current.add(0, entry)
        if (current.size > 100) current.removeAt(current.size - 1)
        _telemetryLogs.value = current
    }

    suspend fun processParsedScreen(
        data: ParsedRideData,
        vehicleId: Long,
        driverId: Long,
        rawHierarchyDump: String? = null
    ) {
        logTelemetry(
            TelemetryLogEntry(
                platform = data.platform,
                eventType = "SCREEN_PARSED",
                detectedStatus = data.status,
                fare = data.fare,
                distanceKm = data.distanceKm,
                snippet = data.rawExtracted?.take(120) ?: "No raw text"
            )
        )

        // Check for orphaned trips first (> 45 min inactive)
        checkOrphanedTrip()

        when (data.status) {
            RideStatus.ACCEPTED -> handleAccepted(data, vehicleId, driverId)
            RideStatus.IN_TRIP -> handleInTrip(data, vehicleId, driverId)
            RideStatus.COMPLETED -> handleCompleted(data, vehicleId, driverId)
            RideStatus.CANCELLED -> handleCancelled(data)
            else -> {
                // Other/Offer states
            }
        }
    }

    private suspend fun handleAccepted(data: ParsedRideData, vehicleId: Long, driverId: Long) {
        val current = _activeTrip.value
        if (current != null && current.status == RideStatus.ACCEPTED) {
            // Already in accepted state, ignore duplicate frames
            return
        }

        val now = Clock.System.now()
        val rideKey = data.rideKey ?: "${data.platform.name.lowercase()}_${vehicleId}_${now.toEpochMilliseconds()}"

        val entity = RideEntity(
            vehicleId = vehicleId,
            driverId = driverId,
            platform = data.platform,
            rideKey = rideKey,
            status = RideStatus.ACCEPTED,
            acceptedAt = now,
            pickup = data.pickup,
            confidence = data.confidence,
            rawExtracted = data.rawExtracted,
            syncStatus = SyncStatus.PENDING,
            createdAt = now
        )

        val insertedId = rideRepository.insertRide(entity)
        if (insertedId > 0) {
            _activeTrip.value = ActiveTripState(
                localRideId = insertedId,
                rideKey = rideKey,
                platform = data.platform,
                status = RideStatus.ACCEPTED,
                acceptedAt = now,
                vehicleId = vehicleId,
                driverId = driverId,
                pickup = data.pickup,
                lastUpdated = now
            )

            rideRepository.recordEvent(
                RideEventEntity(
                    rideId = insertedId,
                    eventType = "ACCEPTED",
                    occurredAt = now,
                    rawJson = """{"platform":"${data.platform.name}","pickup":"${data.pickup ?: ""}"}"""
                )
            )

            Timber.i("🚕 [StateMachine] State moved to ACCEPTED for %s (Ride #%d)", data.platform.displayName, insertedId)
        }
    }

    private suspend fun handleInTrip(data: ParsedRideData, vehicleId: Long, driverId: Long) {
        val current = _activeTrip.value
        val now = Clock.System.now()

        if (current == null) {
            // Recovered or started directly without seeing accept screen
            val rideKey = data.rideKey ?: "${data.platform.name.lowercase()}_${vehicleId}_${now.toEpochMilliseconds()}"
            val entity = RideEntity(
                vehicleId = vehicleId,
                driverId = driverId,
                platform = data.platform,
                rideKey = rideKey,
                status = RideStatus.IN_TRIP,
                acceptedAt = now,
                confidence = 0.9,
                rawExtracted = data.rawExtracted,
                syncStatus = SyncStatus.PENDING,
                createdAt = now
            )
            val insertedId = rideRepository.insertRide(entity)
            if (insertedId > 0) {
                _activeTrip.value = ActiveTripState(
                    localRideId = insertedId,
                    rideKey = rideKey,
                    platform = data.platform,
                    status = RideStatus.IN_TRIP,
                    acceptedAt = now,
                    vehicleId = vehicleId,
                    driverId = driverId,
                    lastUpdated = now
                )
            }
        } else if (current.status == RideStatus.ACCEPTED) {
            _activeTrip.value = current.copy(status = RideStatus.IN_TRIP, lastUpdated = now)
            rideRepository.updateStatus(current.localRideId, RideStatus.IN_TRIP)
            rideRepository.recordEvent(
                RideEventEntity(
                    rideId = current.localRideId,
                    eventType = "IN_TRIP",
                    occurredAt = now,
                    rawJson = """{"status":"IN_TRIP"}"""
                )
            )
            Timber.i("🚕 [StateMachine] State moved to IN_TRIP for %s", current.platform.displayName)
        }
    }

    private suspend fun handleCompleted(data: ParsedRideData, vehicleId: Long, driverId: Long) {
        val current = _activeTrip.value
        val now = Clock.System.now()

        val fare = data.fare
        val distanceKm = data.distanceKm
        val durationMin = data.durationMin ?: 15.0

        // Validation & Confidence Score Calculation
        val isFareValid = fare != null && fare in 15.0..15000.0
        val isDistanceValid = distanceKm == null || distanceKm in 0.2..400.0
        val baseConfidence = data.confidence
        val finalConfidence = if (isFareValid && isDistanceValid) baseConfidence else 0.50
        val needsReview = finalConfidence < 0.80 || !isFareValid

        if (current != null) {
            // Update existing active trip
            rideRepository.completeRide(
                id = current.localRideId,
                completedAt = now,
                fare = fare,
                distanceKm = distanceKm,
                durationMin = durationMin,
                confidence = finalConfidence,
                needsReview = needsReview,
                rawExtracted = data.rawExtracted
            )

            rideRepository.recordEvent(
                RideEventEntity(
                    rideId = current.localRideId,
                    eventType = "COMPLETED",
                    occurredAt = now,
                    rawJson = """{"fare":$fare,"distance":$distanceKm,"duration":$durationMin}"""
                )
            )

            Timber.i("🏁 [StateMachine] COMPLETED Ride #%d (Fare: ₹%.2f, Km: %.1f)", current.localRideId, fare ?: 0.0, distanceKm ?: 0.0)
            _activeTrip.value = null
        } else {
            // New direct completion (e.g. app was opened after trip finished)
            val acceptedAt = now - durationMin.toInt().minutes
            val rideKey = data.rideKey ?: "${data.platform.name.lowercase()}_${vehicleId}_${now.toEpochMilliseconds()}"

            val entity = RideEntity(
                vehicleId = vehicleId,
                driverId = driverId,
                platform = data.platform,
                rideKey = rideKey,
                status = RideStatus.COMPLETED,
                acceptedAt = acceptedAt,
                completedAt = now,
                fare = fare,
                distanceKm = distanceKm,
                durationMin = durationMin,
                pickup = data.pickup,
                dropoff = data.dropoff,
                confidence = finalConfidence,
                needsReview = needsReview,
                reviewNote = if (needsReview) "Low confidence or missing values" else null,
                rawExtracted = data.rawExtracted,
                syncStatus = SyncStatus.PENDING,
                createdAt = now
            )

            val insertedId = rideRepository.insertOrIgnoreRide(entity)
            if (insertedId > 0) {
                rideRepository.recordEvent(
                    RideEventEntity(
                        rideId = insertedId,
                        eventType = "COMPLETED_STANDALONE",
                        occurredAt = now,
                        rawJson = """{"fare":$fare,"distance":$distanceKm}"""
                    )
                )
                Timber.i("🏁 [StateMachine] Standalone COMPLETED captured: ₹%.2f", fare ?: 0.0)
            }
        }
    }

    private suspend fun handleCancelled(data: ParsedRideData) {
        val current = _activeTrip.value ?: return
        val now = Clock.System.now()

        rideRepository.updateStatus(current.localRideId, RideStatus.CANCELLED)
        rideRepository.recordEvent(
            RideEventEntity(
                rideId = current.localRideId,
                eventType = "CANCELLED",
                occurredAt = now,
                rawJson = """{"reason":"Detected from screen"}"""
            )
        )
        Timber.w("❌ [StateMachine] Ride #%d CANCELLED", current.localRideId)
        _activeTrip.value = null
    }

    private suspend fun checkOrphanedTrip() {
        val current = _activeTrip.value ?: return
        val now = Clock.System.now()
        if (now - current.lastUpdated > 45.minutes) {
            Timber.w("⚠️ [StateMachine] Active ride timed out after 45m. Marking ORPHANED.")
            rideRepository.updateStatus(current.localRideId, RideStatus.ORPHANED)
            rideRepository.recordEvent(
                RideEventEntity(
                    rideId = current.localRideId,
                    eventType = "ORPHANED_TIMEOUT",
                    occurredAt = now,
                    rawJson = """{"reason":"Timeout > 45 minutes"}"""
                )
            )
            _activeTrip.value = null
        }
    }
}

