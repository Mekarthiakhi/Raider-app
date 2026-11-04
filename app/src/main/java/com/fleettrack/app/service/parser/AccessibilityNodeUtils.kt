package com.fleettrack.app.service.parser

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

object AccessibilityNodeUtils {

    private val FARE_REGEXES = listOf(
        Regex("""₹\s*([0-9]+(?:\.[0-9]{1,2})?)"""),
        Regex("""(?:Rs\.?|INR)\s*([0-9]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""(?:Collect|Cash|Fare|Total|Earnings|Collected|Received|किराया|ఛార్జీ)\s*[:\-]?\s*₹?\s*([0-9]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""^\s*([0-9]+(?:\.[0-9]{2}))\s*$""") // Standalone currency decimal like 240.50
    )

    private val DISTANCE_REGEX = Regex("""([0-9]+(?:\.[0-9]+)?)\s*(?:km|kms|kilometers|किमी|కిమీ)\b""", RegexOption.IGNORE_CASE)
    private val DURATION_REGEX = Regex("""([0-9]+)\s*(?:min|mins|minutes|मिनट|నిమిషాలు)\b""", RegexOption.IGNORE_CASE)

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

    /**
     * Finds nodes matching a target view ID (full name or relative ID).
     */
    fun findNodesByViewId(root: AccessibilityNodeInfo?, targetId: String): List<AccessibilityNodeInfo> {
        if (root == null || targetId.isEmpty()) return emptyList()
        val matches = mutableListOf<AccessibilityNodeInfo>()

        try {
            val found = root.findAccessibilityNodeInfosByViewId(targetId)
            if (!found.isNullOrEmpty()) {
                matches.addAll(found)
            }
        } catch (_: Exception) {}

        if (matches.isEmpty()) {
            searchNodeRecursive(root, targetId, matches)
        }
        return matches
    }

    private fun searchNodeRecursive(node: AccessibilityNodeInfo, targetId: String, matches: MutableList<AccessibilityNodeInfo>) {
        val resName = node.viewIdResourceName ?: ""
        if (resName.equals(targetId, ignoreCase = true) || 
            resName.endsWith("/$targetId") || 
            resName.substringAfterLast(":id/") == targetId) {
            matches.add(node)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            searchNodeRecursive(child, targetId, matches)
        }
    }

    /**
     * Iterates over a candidate list of view IDs and returns the first non-blank text or contentDescription found.
     */
    fun findTextByViewIds(root: AccessibilityNodeInfo?, viewIds: List<String>): String? {
        if (root == null || viewIds.isEmpty()) return null
        for (vid in viewIds) {
            val nodes = findNodesByViewId(root, vid)
            for (node in nodes) {
                val t = node.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
                if (t != null) return t
                val d = node.contentDescription?.toString()?.trim()?.takeIf { it.isNotEmpty() }
                if (d != null) return d
            }
        }
        return null
    }

    /**
     * Dumps the full node tree as human-readable hierarchy with view IDs and text.
     * Perfect for debugging, testing on real phones, and sharing node dumps.
     */
    fun dumpNodeHierarchy(root: AccessibilityNodeInfo?): String {
        if (root == null) return "Empty root node"
        val sb = StringBuilder()
        dumpNodeRecursive(root, sb, 0)
        return sb.toString()
    }

    private fun dumpNodeRecursive(node: AccessibilityNodeInfo, sb: StringBuilder, depth: Int) {
        val indent = "  ".repeat(depth)
        val viewId = node.viewIdResourceName?.substringAfterLast(":id/") ?: ""
        val text = node.text?.toString()?.trim()?.replace("\n", " ") ?: ""
        val desc = node.contentDescription?.toString()?.trim()?.replace("\n", " ") ?: ""
        val className = node.className?.toString()?.substringAfterLast(".") ?: ""

        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        sb.append(indent)
            .append("• <")
            .append(className)
            .append(">")

        if (viewId.isNotEmpty()) {
            sb.append(" id=").append(viewId)
        }
        if (text.isNotEmpty()) {
            sb.append(" text=\"").append(text).append("\"")
        }
        if (desc.isNotEmpty() && desc != text) {
            sb.append(" desc=\"").append(desc).append("\"")
        }
        sb.append("\n")

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            dumpNodeRecursive(child, sb, depth + 1)
        }
    }

    fun extractFare(texts: List<String>): Double? {
        for (text in texts) {
            for (regex in FARE_REGEXES) {
                val match = regex.find(text)
                if (match != null) {
                    val amount = match.groupValues[1].toDoubleOrNull()
                    if (amount != null && amount in 15.0..15000.0) {
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
                if (km != null && km in 0.2..400.0) {
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
