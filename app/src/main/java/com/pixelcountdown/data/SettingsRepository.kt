package com.pixelcountdown.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(prefs.getString(KEY_THEME, "system") ?: "system")
    val theme: StateFlow<String> = _theme.asStateFlow()

    private val _fontFamily = MutableStateFlow(prefs.getString(KEY_FONT_FAMILY, "default") ?: "default")
    val fontFamily: StateFlow<String> = _fontFamily.asStateFlow()

    fun setTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
        _theme.value = theme
    }

    fun setFontFamily(font: String) {
        prefs.edit().putString(KEY_FONT_FAMILY, font).apply()
        _fontFamily.value = font
    }

    companion object {
        private const val KEY_THEME = "pref_theme"
        private const val KEY_FONT_FAMILY = "pref_font_family"

        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
