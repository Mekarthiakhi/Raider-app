package com.fleettrack.app.service.parser

import android.view.accessibility.AccessibilityNodeInfo
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RapidoCaptainParser @Inject constructor(
    private val dynamicSelectorEngine: DynamicSelectorEngine
) : ScreenParser {

    override val platform: Platform = Platform.RAPIDO

    private val completedKeywords = listOf(
        "ride completed", "collect cash", "payment received", "total fare",
        "fare summary", "cash to collect", "online payment", "ride summary",
        "collect", "ride ended", "customer paid", "किराया", "ఛార్జీ"
    )

    private val inTripKeywords = listOf(
        "ride started", "end ride", "drop customer", "navigating to drop",
        "rapido auto", "rapido cab", "rapido bike", "captain", "drop location"
    )

    private val acceptedKeywords = listOf(
        "navigate to pickup", "arrived at pickup", "start ride", "pickup customer",
        "enter otp", "start trip", "call rider", "reach pickup", "पिकअप", "పికప్"
    )

    private val cancelledKeywords = listOf(
        "ride cancelled", "cancelled by customer", "cancelled by rider", "trip cancelled",
        "order cancelled", "cancellation fee", "रद्द", "రద్దు"
    )

    override fun canHandle(packageName: String): Boolean {
        return platform.primaryPackage == packageName || platform.alternativePackages.contains(packageName)
    }

    override fun parse(rootNode: AccessibilityNodeInfo): ParsedRideData? {
        val texts = AccessibilityNodeUtils.extractAllTexts(rootNode)
        if (texts.isEmpty()) return null

        // 1. Evaluate OTA Remote Selectors first
        val dynamicResult = dynamicSelectorEngine.evaluate(platform, rootNode, texts)
        if (dynamicResult != null) {
            return dynamicResult
        }

        // 2. Static heuristic fallback
        val lowerJoined = texts.joinToString(" ").lowercase()

        val isCompleted = completedKeywords.any { lowerJoined.contains(it) }
        val isCancelled = cancelledKeywords.any { lowerJoined.contains(it) }
        val isInTrip = inTripKeywords.any { lowerJoined.contains(it) }
        val isAccepted = acceptedKeywords.any { lowerJoined.contains(it) }

        val fare = AccessibilityNodeUtils.extractFare(texts)
        val distance = AccessibilityNodeUtils.extractDistanceKm(texts)
        val duration = AccessibilityNodeUtils.extractDurationMin(texts)

        return when {
            isCompleted && fare != null -> {
                val minuteWindow = System.currentTimeMillis() / 60000
                val rideKey = "rapido_${minuteWindow}_${fare.toInt()}"

                ParsedRideData(
                    platform = Platform.RAPIDO,
                    status = RideStatus.COMPLETED,
                    fare = fare,
                    distanceKm = distance,
                    durationMin = duration,
                    rideKey = rideKey,
                    confidence = 1.0,
                    rawExtracted = texts.take(15).joinToString(" | ")
                )
            }

            isCancelled -> {
                ParsedRideData(
                    platform = Platform.RAPIDO,
                    status = RideStatus.CANCELLED,
                    confidence = 0.95,
                    rawExtracted = texts.take(10).joinToString(" | ")
                )
            }

            isInTrip -> {
                ParsedRideData(
                    platform = Platform.RAPIDO,
                    status = RideStatus.IN_TRIP,
                    distanceKm = distance,
                    confidence = 0.95,
                    rawExtracted = texts.take(10).joinToString(" | ")
                )
            }

            isAccepted -> {
                ParsedRideData(
                    platform = Platform.RAPIDO,
                    status = RideStatus.ACCEPTED,
                    confidence = 0.95,
                    rawExtracted = texts.take(10).joinToString(" | ")
                )
            }

            else -> null
        }
    }
}
