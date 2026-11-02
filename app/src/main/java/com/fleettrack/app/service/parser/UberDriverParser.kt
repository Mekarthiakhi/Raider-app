package com.fleettrack.app.service.parser

import android.view.accessibility.AccessibilityNodeInfo
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UberDriverParser @Inject constructor() : ScreenParser {

    override val platform: Platform = Platform.UBER

    private val completedKeywords = listOf(
        "trip earnings", "collect cash", "cash trip", "trip balance",
        "trip completed", "cash collected", "you earned", "complete trip",
        "payment details", "trip fare", "cash received", "done"
    )

    private val inTripKeywords = listOf(
        "dropping off", "pickup", "navigating", "end trip", "complete uber",
        "ubergo", "uber premier", "uberxl", "uber auto", "rider on board"
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
                // Trip completed with fare extracted
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

            isInTrip -> {
                ParsedRideData(
                    platform = Platform.UBER,
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

