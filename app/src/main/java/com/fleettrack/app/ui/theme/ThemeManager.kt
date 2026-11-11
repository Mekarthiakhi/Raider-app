package com.fleettrack.app.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Shared reactive theme state persisted in SharedPreferences.
 */
object ThemeManager {
    private const val PREFS_NAME = "svm_fleet_prefs"
    private const val KEY_DARK_THEME = "dark_mode_enabled"

    var isDarkTheme by mutableStateOf(false)
        private set

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isDarkTheme = prefs.getBoolean(KEY_DARK_THEME, false)
        initialized = true
    }

    fun setDarkTheme(context: Context, enabled: Boolean) {
        isDarkTheme = enabled
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
        initialized = true
    }
}

