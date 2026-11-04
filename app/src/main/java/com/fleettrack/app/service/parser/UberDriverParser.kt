package com.fleettrack.app.service.parser

import android.view.accessibility.AccessibilityNodeInfo
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UberDriverParser @Inject constructor(
    private val dynamicSelectorEngine: DynamicSelectorEngine
) : ScreenParser {

    override val platform: Platform = Platform.UBER

    private val completedKeywords = listOf(
        "trip balance", "total payout", "your earnings", "fare", "cash collected",
        "trip completed", "rate rider", "paid with cash", "receipt", "earning for this trip",
        "customer paid", "किराया", "कमाई"
    )

    private val inTripKeywords = listOf(
        "drop off", "navigate", "on trip", "ubergo", "uberpremier", "uberauto",
        "arrived at destination", "completing trip", "slide to complete"
    )

    private val acceptedKeywords = listOf(
        "navigate to pickup", "picking up", "arrived", "start uber", "slide to start",
        "confirm pickup", "arriving", "rider is waiting"
    )

    private val cancelledKeywords = listOf(
        "trip was cancelled", "cancelled by rider", "rider cancelled", "cancellation fee",
        "trip cancelled"
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
                val rideKey = "uber_${minuteWindow}_${fare.toInt()}"

                ParsedRideData(
                    platform = Platform.UBER,
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
                    platform = Platform.UBER,
                    status = RideStatus.CANCELLED,
                    confidence = 0.95,
                    rawExtracted = texts.take(10).joinToString(" | ")
                )
            }

            isInTrip -> {
                ParsedRideData(
                    platform = Platform.UBER,
                    status = RideStatus.IN_TRIP,
                    distanceKm = distance,
                    confidence = 0.95,
                    rawExtracted = texts.take(10).joinToString(" | ")
                )
            }

            isAccepted -> {
                ParsedRideData(
                    platform = Platform.UBER,
                    status = RideStatus.ACCEPTED,
                    confidence = 0.95,
                    rawExtracted = texts.take(10).joinToString(" | ")
                )
            }

            else -> null
        }
    }
}
