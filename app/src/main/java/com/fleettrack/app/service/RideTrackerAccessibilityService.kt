package com.fleettrack.app.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import com.fleettrack.app.data.model.SyncStatus
import com.fleettrack.app.data.repository.RideRepository
import com.fleettrack.app.data.session.ShiftManager
import com.fleettrack.app.service.parser.OcrFallbackEngine
import com.fleettrack.app.service.parser.OlaDriverParser
import com.fleettrack.app.service.parser.ParsedRideData
import com.fleettrack.app.service.parser.RapidoCaptainParser
import com.fleettrack.app.service.parser.UberDriverParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

@AndroidEntryPoint
class RideTrackerAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var shiftManager: ShiftManager

    @Inject
    lateinit var rideRepository: RideRepository

    @Inject
    lateinit var uberParser: UberDriverParser

    @Inject
    lateinit var rapidoParser: RapidoCaptainParser

    @Inject
    lateinit var olaParser: OlaDriverParser

    @Inject
    lateinit var ocrFallbackEngine: OcrFallbackEngine

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Debounce timestamp to avoid re-parsing the exact same frame repeatedly within 2 seconds
    private var lastParseTimestamp: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return

        // DPDP Act Compliance: Strictly ignore any non-fleet app
        val parser = when {
            uberParser.canHandle(packageName) -> uberParser
            rapidoParser.canHandle(packageName) -> rapidoParser
            olaParser.canHandle(packageName) -> olaParser
            else -> return
        }

        val now = System.currentTimeMillis()
        if (now - lastParseTimestamp < 1500) {
            return
        }
        lastParseTimestamp = now

        val rootNode = rootInActiveWindow ?: return

        serviceScope.launch {
            try {
                // 1. Attempt Node Hierarchy Extraction (Fast & Efficient)
                val parsedData = parser.parse(rootNode)

                if (parsedData != null && parsedData.status == RideStatus.COMPLETED && parsedData.fare != null) {
                    recordCompletedRide(parsedData)
                } else if (parsedData == null && isCompletedScreenHeuristic(rootNode)) {
                    // 2. Fallback to ML Kit OCR if view IDs are obfuscated on completion screen
                    triggerOcrFallback(parser.platform)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error processing accessibility telemetry event for %s", packageName)
            }
        }
    }

    private fun isCompletedScreenHeuristic(rootNode: android.view.accessibility.AccessibilityNodeInfo): Boolean {
        val texts = com.fleettrack.app.service.parser.AccessibilityNodeUtils.extractAllTexts(rootNode)
        val joined = texts.joinToString(" ").lowercase()
        return joined.contains("collect") || joined.contains("completed") || joined.contains("fare") || joined.contains("earnings")
    }

    private fun triggerOcrFallback(platform: Platform) {
        takeScreenshot(
            Display.DEFAULT_DISPLAY,
            mainExecutor,
            object : TakeScreenshotCallback {
                override fun onSuccess(screenshotResult: ScreenshotResult) {
                    val hardwareBuffer = screenshotResult.hardwareBuffer
                    val colorSpace = screenshotResult.colorSpace
                    val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)?.copy(Bitmap.Config.ARGB_8888, false)
                    hardwareBuffer.close()

                    if (bitmap != null) {
                        serviceScope.launch {
                            val ocrData = ocrFallbackEngine.extractFromBitmap(bitmap, platform)
                            if (ocrData != null && ocrData.fare != null) {
                                recordCompletedRide(ocrData)
                            }
                        }
                    }
                }

                override fun onFailure(errorCode: Int) {
                    Timber.w("Accessibility takeScreenshot failed with error code: %d", errorCode)
                }
            }
        )
    }

    private suspend fun recordCompletedRide(data: ParsedRideData) {
        val shift = shiftManager.currentShift.first()
        val vehicleId = shift.vehicleId ?: 1L
        val driverId = shift.driverId ?: 1L

        val durationMinutes = data.durationMin ?: 15.0
        val acceptedAt = Clock.System.now() - durationMinutes.toInt().minutes
        val completedAt = Clock.System.now()

        val rideKey = data.rideKey ?: "${data.platform.name.lowercase()}_${vehicleId}_${completedAt.toEpochMilliseconds()}"

        val ride = RideEntity(
            vehicleId = vehicleId,
            driverId = driverId,
            platform = data.platform,
            rideKey = rideKey,
            status = RideStatus.COMPLETED,
            acceptedAt = acceptedAt,
            completedAt = completedAt,
            fare = data.fare,
            distanceKm = data.distanceKm,
            durationMin = durationMinutes,
            pickup = data.pickup,
            dropoff = data.dropoff,
            confidence = data.confidence,
            needsReview = data.confidence < 0.8,
            reviewNote = if (data.confidence < 0.8) "Parsed via ML Kit OCR" else null,
            rawExtracted = data.rawExtracted,
            syncStatus = SyncStatus.PENDING,
            createdAt = completedAt
        )

        val insertedId = rideRepository.insertOrIgnoreRide(ride)
        if (insertedId > 0) {
            Timber.i(
                "✅ Captured %s trip: ₹%.2f (%.1f km) for Cab #%d, Driver #%d",
                data.platform.displayName,
                data.fare,
                data.distanceKm ?: 0.0,
                vehicleId,
                driverId
            )
        }
    }

    override fun onInterrupt() {
        Timber.w("RideTrackerAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Timber.i("RideTrackerAccessibilityService destroyed")
    }
}

