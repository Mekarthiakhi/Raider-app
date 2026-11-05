package com.fleettrack.app.ui.driver.setup

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SetupState(
    val isAccessibilityEnabled: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false,
    val isRestrictedSettingsAllowed: Boolean = true, // Android 13+ check
    val isOemAutoStartConfigured: Boolean = false
)

@HiltViewModel
class SetupWizardViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SetupState())
    val state: StateFlow<SetupState> = _state.asStateFlow()

    fun checkPermissions() {
        try {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            val enabledServices = am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK) ?: emptyList()
            val isA11yOn = enabledServices.any {
                it.resolveInfo?.serviceInfo?.packageName == context.packageName
            }

            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val isBatteryIgnored = pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false

            _state.value = SetupState(
                isAccessibilityEnabled = isA11yOn,
                isBatteryOptimizationIgnored = isBatteryIgnored,
                isRestrictedSettingsAllowed = true, // Requires user confirmation if prompted
                isOemAutoStartConfigured = false
            )
        } catch (e: Exception) {
            _state.value = SetupState(
                isAccessibilityEnabled = false,
                isBatteryOptimizationIgnored = false,
                isRestrictedSettingsAllowed = true,
                isOemAutoStartConfigured = false
            )
        }
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openBatterySettings(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

