package com.fleettrack.app.service.parser

import android.view.accessibility.AccessibilityNodeInfo
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.data.model.RideStatus
import com.fleettrack.app.data.remote.RemoteSelectorDataSource
import com.fleettrack.app.data.remote.RemoteSelectorDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicSelectorEngine @Inject constructor(
    private val remoteSelectorDataSource: RemoteSelectorDataSource
) {
    private val mutex = Mutex()
    private val cachedSelectors = mutableMapOf<String, List<RemoteSelectorDto>>()

    /**
     * Non-blocking background sync of remote selectors for all platforms.
     */
    fun syncSelectorsAsync(scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        scope.launch {
            syncSelectors()
        }
    }

    /**
     * Fetches and caches active remote selectors from Supabase.
     */
    suspend fun syncSelectors() {
        for (p in listOf("rapido", "uber", "ola")) {
            val result = remoteSelectorDataSource.fetchSelectors(p)
            result.onSuccess { list ->
                if (list.isNotEmpty()) {
                    mutex.withLock {
                        cachedSelectors[p] = list.sortedByDescending { it.priority }
                    }
                    Timber.d("DynamicSelectorEngine: Cached %d remote selectors for %s", list.size, p)
                }
            }
        }
    }

    /**
     * Evaluates active remote selectors against the current accessibility node tree and texts.
     * Returns ParsedRideData if a remote selector matches; otherwise null (caller should fall back to static rules).
     */
    fun evaluate(
        platform: Platform,
        rootNode: AccessibilityNodeInfo,
        texts: List<String>
    ): ParsedRideData? {
        val platformKey = when (platform) {
            Platform.RAPIDO -> "rapido"
            Platform.UBER -> "uber"
            Platform.OLA -> "ola"
            else -> return null
        }

        val selectors = cachedSelectors[platformKey]
        if (selectors.isNullOrEmpty()) return null

        val joinedLowerTexts = texts.joinToString(" ").lowercase()

        for (selector in selectors) {
            if (!selector.active) continue
            val config = selector.selectors ?: continue

            val screenType = selector.screenType.lowercase()

            when (screenType) {
                "completed" -> {
                    val fields = config["fields"]?.jsonObject ?: continue
                    val fare = extractField(rootNode, texts, fields["fare"]?.jsonObject)
                    if (fare != null && fare in 15.0..15000.0) {
                        val distance = extractField(rootNode, texts, fields["distance_km"]?.jsonObject)
                        val duration = extractField(rootNode, texts, fields["duration_min"]?.jsonObject)
                        val minuteWindow = System.currentTimeMillis() / 60000
                        val rideKey = "${platformKey}_${minuteWindow}_${fare.toInt()}"

                        return ParsedRideData(
                            platform = platform,
                            status = RideStatus.COMPLETED,
                            fare = fare,
                            distanceKm = distance,
                            durationMin = duration,
                            rideKey = rideKey,
                            confidence = 1.0,
                            rawExtracted = texts.take(15).joinToString(" | ")
                        )
                    }
                }
                "accepted" -> {
                    val triggerKeywords = extractKeywords(config["trigger_keywords"])
                    val isTriggered = triggerKeywords.any { joinedLowerTexts.contains(it.lowercase()) }
                    if (isTriggered) {
                        val pickup = extractText(rootNode, config["fields"]?.jsonObject?.get("pickup")?.jsonObject)
                        return ParsedRideData(
                            platform = platform,
                            status = RideStatus.ACCEPTED,
                            pickup = pickup,
                            confidence = 0.95,
                            rawExtracted = texts.take(10).joinToString(" | ")
                        )
                    }
                }
                "in_trip" -> {
                    val triggerKeywords = extractKeywords(config["trigger_keywords"])
                    val isTriggered = triggerKeywords.any { joinedLowerTexts.contains(it.lowercase()) }
                    if (isTriggered) {
                        return ParsedRideData(
                            platform = platform,
                            status = RideStatus.IN_TRIP,
                            confidence = 0.95,
                            rawExtracted = texts.take(10).joinToString(" | ")
                        )
                    }
                }
                "cancelled" -> {
                    val triggerKeywords = extractKeywords(config["trigger_keywords"])
                    val isTriggered = triggerKeywords.any { joinedLowerTexts.contains(it.lowercase()) }
                    if (isTriggered) {
                        return ParsedRideData(
                            platform = platform,
                            status = RideStatus.CANCELLED,
                            confidence = 0.95,
                            rawExtracted = texts.take(10).joinToString(" | ")
                        )
                    }
                }
            }
        }

        return null
    }

    private fun extractField(
        rootNode: AccessibilityNodeInfo,
        texts: List<String>,
        fieldConfig: JsonObject?
    ): Double? {
        if (fieldConfig == null) return null

        // 1. Try View IDs first
        val viewIds = fieldConfig["view_ids"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
        val textFromNode = AccessibilityNodeUtils.findTextByViewIds(rootNode, viewIds)
        if (textFromNode != null) {
            val parsed = parseValueWithPatterns(listOf(textFromNode), fieldConfig["regex_patterns"]?.jsonArray)
            if (parsed != null) return parsed
        }

        // 2. Try Regex Patterns across all extracted texts
        return parseValueWithPatterns(texts, fieldConfig["regex_patterns"]?.jsonArray)
    }

    private fun extractText(
        rootNode: AccessibilityNodeInfo,
        fieldConfig: JsonObject?
    ): String? {
        if (fieldConfig == null) return null
        val viewIds = fieldConfig["view_ids"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
        return AccessibilityNodeUtils.findTextByViewIds(rootNode, viewIds)
    }

    private fun parseValueWithPatterns(texts: List<String>, patternsArray: JsonArray?): Double? {
        val patterns = patternsArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
        for (patternStr in patterns) {
            val regex = try { Regex(patternStr, RegexOption.IGNORE_CASE) } catch (_: Exception) { null } ?: continue
            for (text in texts) {
                val match = regex.find(text)
                if (match != null && match.groupValues.size > 1) {
                    val rawVal = match.groupValues[1].replace(",", "").trim()
                    val d = rawVal.toDoubleOrNull()
                    if (d != null) return d
                }
            }
        }
        return null
    }

    private fun extractKeywords(element: JsonElement?): List<String> {
        if (element == null) return emptyList()
        val list = mutableListOf<String>()
        if (element is JsonObject) {
            for ((_, arr) in element) {
                if (arr is JsonArray) {
                    for (item in arr) {
                        item.jsonPrimitive.contentOrNull?.let { list.add(it) }
                    }
                }
            }
        } else if (element is JsonArray) {
            for (item in element) {
                item.jsonPrimitive.contentOrNull?.let { list.add(it) }
            }
        }
        return list
    }
}
