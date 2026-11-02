package com.fleettrack.app.service.parser

import android.view.accessibility.AccessibilityNodeInfo

object AccessibilityNodeUtils {

    private val FARE_REGEXES = listOf(
        Regex("""₹\s*([0-9]+(?:\.[0-9]{1,2})?)"""),
        Regex("""(?:Rs\.?|INR)\s*([0-9]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""(?:Collect|Cash|Fare|Total|Earnings|Collected|Received)\s*[:\-]?\s*₹?\s*([0-9]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""^\s*([0-9]+(?:\.[0-9]{2}))\s*$""") // Standalone currency decimal like 240.50
    )

    private val DISTANCE_REGEX = Regex("""([0-9]+(?:\.[0-9]+)?)\s*(?:km|kms|kilometers)\b""", RegexOption.IGNORE_CASE)
    private val DURATION_REGEX = Regex("""([0-9]+)\s*(?:min|mins|minutes)\b""", RegexOption.IGNORE_CASE)

    fun extractAllTexts(root: AccessibilityNodeInfo?): List<String> {
        if (root == null) return emptyList()
        val results = mutableListOf<String>()
        traverseNode(root, results)
        return results
    }

    private fun traverseNode(node: AccessibilityNodeInfo, list: MutableList<String>) {
        node.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let { list.add(it) }
        node.contentDescription?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let { list.add(it) }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            traverseNode(child, list)
        }
    }

    fun extractFare(texts: List<String>): Double? {
        // First look for explicit ₹ or Rs labels
        for (text in texts) {
            for (regex in FARE_REGEXES) {
                val match = regex.find(text)
                if (match != null) {
                    val amount = match.groupValues[1].toDoubleOrNull()
                    if (amount != null && amount in 15.0..10000.0) {
                        return amount
                    }
                }
            }
        }
        return null
    }

    fun extractDistanceKm(texts: List<String>): Double? {
        for (text in texts) {
            val match = DISTANCE_REGEX.find(text)
            if (match != null) {
                val km = match.groupValues[1].toDoubleOrNull()
                if (km != null && km in 0.2..300.0) {
                    return km
                }
            }
        }
        return null
    }

    fun extractDurationMin(texts: List<String>): Double? {
        for (text in texts) {
            val match = DURATION_REGEX.find(text)
            if (match != null) {
                val mins = match.groupValues[1].toDoubleOrNull()
                if (mins != null && mins in 1.0..600.0) {
                    return mins
                }
            }
        }
        return null
    }
}

