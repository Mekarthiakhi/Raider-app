package com.fleettrack.app.service.parser

import android.view.accessibility.AccessibilityNodeInfo
import com.fleettrack.app.data.model.Platform

interface ScreenParser {
    val platform: Platform
    fun canHandle(packageName: String): Boolean
    fun parse(rootNode: AccessibilityNodeInfo): ParsedRideData?
}

