package com.fleettrack.app.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import com.fleettrack.app.data.session.ShiftManager
import com.fleettrack.app.service.parser.AccessibilityNodeUtils
import com.fleettrack.app.service.parser.OcrFallbackEngine
import com.fleettrack.app.service.parser.OlaDriverParser
import com.fleettrack.app.service.parser.ParsedRideData
import com.fleettrack.app.service.parser.RapidoCaptainParser
import com.fleettrack.app.service.parser.UberDriverParser
import com.fleettrack.app.service.statemachine.RideStateMachine
import com.fleettrack.app.service.statemachine.TelemetryLogEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class RideTrackerAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var shiftManager: ShiftManager

    @Inject
    lateinit var rideStateMachine: RideStateMachine

    @Inject
    lateinit var uberParser: UberDriverParser

    @Inject
    lateinit var rapidoParser: RapidoCaptainParser

    @Inject
    lateinit var olaParser: OlaDriverParser

    @Inject
    lateinit var ocrFallbackEngine: OcrFallbackEngine

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Debounce timestamp to avoid re-parsing the exact same frame repeatedly within 1.2 seconds
    private var lastParseTimestamp: Long = 0L

    companion object {
        var isServiceRunning: Boolean = false
            private set

        var lastActiveWindowDump: String = ""
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        Timber.i("RideTrackerAccessibilityService connected")
    }

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
        if (now - lastParseTimestamp < 1200) {
            return
        }
        lastParseTimestamp = now

        val rootNode = rootInActiveWindow ?: return

        serviceScope.launch {
            try {
                // Update live window dump for Debug Node Inspector
                val hierarchyDump = AccessibilityNodeUtils.dumpNodeHierarchy(rootNode)
                lastActiveWindowDump = hierarchyDump

                val shift = shiftManager.currentShift.first()
                val vehicleId = shift.vehicleId ?: 1L
                val driverId = shift.driverId ?: 1L

                // 1. Attempt Node Hierarchy Extraction (Fast & Efficient)
                val parsedData = parser.parse(rootNode)

                if (parsedData != null) {
                    rideStateMachine.processParsedScreen(
                        data = parsedData,
                        vehicleId = vehicleId,
                        driverId = driverId,
                        rawHierarchyDump = hierarchyDump
                    )
                } else if (isCompletedScreenHeuristic(rootNode)) {
                    // 2. Fallback to ML Kit OCR if view IDs are obfuscated on completion screen
                    triggerOcrFallback(parser.platform, vehicleId, driverId)
                } else {
                    rideStateMachine.logTelemetry(
                        TelemetryLogEntry(
                            platform = parser.platform,
                            eventType = "WINDOW_CHANGE",
                            detectedStatus = null,
                            snippet = "Package: $packageName | Elements: ${rootNode.childCount}"
                        )
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error processing accessibility telemetry event for %s", packageName)
            }
        }
    }

    private fun isCompletedScreenHeuristic(rootNode: android.view.accessibility.AccessibilityNodeInfo): Boolean {
        val texts = AccessibilityNodeUtils.extractAllTexts(rootNode)
        val joined = texts.joinToString(" ").lowercase()
        return joined.contains("collect") || joined.contains("completed") || joined.contains("fare") || joined.contains("earnings")
    }

    private fun triggerOcrFallback(platform: Platform, vehicleId: Long, driverId: Long) {
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
                                rideStateMachine.processParsedScreen(
                                    data = ocrData,
                                    vehicleId = vehicleId,
                                    driverId = driverId,
                                    rawHierarchyDump = "ML Kit OCR Extraction"
                                )
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

    override fun onInterrupt() {
        Timber.w("RideTrackerAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        serviceScope.cancel()
        Timber.i("RideTrackerAccessibilityService destroyed")
    }
}
