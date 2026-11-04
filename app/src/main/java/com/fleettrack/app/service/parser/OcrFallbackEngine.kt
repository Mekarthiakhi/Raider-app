package com.fleettrack.app.service.parser

import android.graphics.Bitmap
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class OcrFallbackEngine @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractFromBitmap(bitmap: Bitmap, platform: Platform): ParsedRideData? {
        return try {
            val text = recognizeText(bitmap)
            if (text.isBlank()) return null

            val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
            val fare = AccessibilityNodeUtils.extractFare(lines)
            val distance = AccessibilityNodeUtils.extractDistanceKm(lines)
            val duration = AccessibilityNodeUtils.extractDurationMin(lines)

            if (fare != null) {
                val minuteWindow = System.currentTimeMillis() / 60000
                val rideKey = "${platform.name.lowercase()}_ocr_${minuteWindow}_${fare.toInt()}"

                ParsedRideData(
                    platform = platform,
                    status = RideStatus.COMPLETED,
                    fare = fare,
                    distanceKm = distance,
                    durationMin = duration,
                    rideKey = rideKey,
                    confidence = 0.8, // Flagged for review since extracted via OCR fallback
                    rawExtracted = "OCR: " + lines.take(8).joinToString(" | ")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "OCR fallback processing failed")
            null
        }
    }

    private suspend fun recognizeText(bitmap: Bitmap): String = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                cont.resume(visionText.text)
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }
}

