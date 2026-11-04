package com.fleettrack.app.service.parser

import android.view.accessibility.AccessibilityNodeInfo
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OlaDriverParser @Inject constructor(
    private val dynamicSelectorEngine: DynamicSelectorEngine
) : ScreenParser {

    override val platform: Platform = Platform.OLA

    private val completedKeywords = listOf(
        "total bill", "ride fare", "cash to collect", "online payment", "ola money",
        "trip completed", "collect", "bill details", "fare breakdown", "total payable",
        "customer paid", "किराया", "మొత్తం ఛార్జీ"
    )

    private val inTripKeywords = listOf(
        "end trip", "drop", "navigating", "ola mini", "ola prime", "ola auto",
        "destination", "heading to destination"
    )

    private val acceptedKeywords = listOf(
        "go to pickup", "start ride", "enter otp", "arrived", "client pickup",
        "reach pickup", "start trip"
    )

    private val cancelledKeywords = listOf(
        "ride cancelled", "cancelled by customer", "trip cancelled", "order cancelled"
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
                val rideKey = "ola_${minuteWindow}_${fare.toInt()}"

                ParsedRideData(
                    platform = Platform.OLA,
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
                    platform = Platform.OLA,
                    status = RideStatus.CANCELLED,
                    confidence = 0.95,
                    rawExtracted = texts.take(10).joinToString(" | ")
                )
            }

            isInTrip -> {
                ParsedRideData(
                    platform = Platform.OLA,
                    status = RideStatus.IN_TRIP,
                    distanceKm = distance,
                    confidence = 0.95,
                    rawExtracted = texts.take(10).joinToString(" | ")
                )
            }

            isAccepted -> {
                ParsedRideData(
                    platform = Platform.OLA,
                    status = RideStatus.ACCEPTED,
                    confidence = 0.95,
                    rawExtracted = texts.take(10).joinToString(" | ")
                )
            }

            else -> null
        }
    }
}
