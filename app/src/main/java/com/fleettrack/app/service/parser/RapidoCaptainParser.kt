package com.fleettrack.app.service.parser

import android.view.accessibility.AccessibilityNodeInfo
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RapidoCaptainParser @Inject constructor() : ScreenParser {

    override val platform: Platform = Platform.RAPIDO

    private val completedKeywords = listOf(
        "ride completed", "collect cash", "payment received", "total fare",
        "fare summary", "cash to collect", "online payment", "ride summary",
        "collect", "ride ended", "customer paid"
    )

    private val inTripKeywords = listOf(
        "ride started", "end ride", "drop customer", "navigating to drop",
        "rapido auto", "rapido cab", "rapido bike", "captain"
    )

    override fun canHandle(packageName: String): Boolean {
        return platform.primaryPackage == packageName || platform.alternativePackages.contains(packageName)
    }

    override fun parse(rootNode: AccessibilityNodeInfo): ParsedRideData? {
        val texts = AccessibilityNodeUtils.extractAllTexts(rootNode)
        if (texts.isEmpty()) return null

        val lowerJoined = texts.joinToString(" ").lowercase()

        val isCompleted = completedKeywords.any { lowerJoined.contains(it) }
        val isInTrip = inTripKeywords.any { lowerJoined.contains(it) }

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

            isInTrip -> {
                ParsedRideData(
                    platform = Platform.RAPIDO,
                    status = RideStatus.IN_TRIP,
                    distanceKm = distance,
                    durationMin = duration,
                    confidence = 0.9,
                    rawExtracted = texts.take(10).joinToString(" | ")
                )
            }

            else -> null
        }
    }
}

