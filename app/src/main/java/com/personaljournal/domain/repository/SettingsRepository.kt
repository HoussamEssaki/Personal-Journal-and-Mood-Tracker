package com.personaljournal.domain.repository

import com.personaljournal.domain.model.AccessibilityPreferences
import com.personaljournal.domain.model.LayoutPreferences
import com.personaljournal.domain.model.PersonalPreferences
import com.personaljournal.domain.model.QuickCaptureTarget
import com.personaljournal.domain.model.DensityMode
import com.personaljournal.domain.model.LayoutPreset
import kotlinx.coroutines.flow.Flow

const val DEFAULT_ACCENT_HEX = "#4C7DFF"

enum class ThemeMode { LIGHT, DARK, SYSTEM }

interface SettingsRepository {
    fun themeMode(): Flow<ThemeMode>
    suspend fun setTheme(mode: ThemeMode)
    fun language(): Flow<String>
    suspend fun setLanguage(languageTag: String)
    fun accentColor(): Flow<String>
    suspend fun setAccentColor(colorHex: String)

    fun layoutPreferences(): Flow<LayoutPreferences>
    suspend fun setLayoutPreset(preset: LayoutPreset)
    suspend fun setDensityMode(mode: DensityMode)

    fun personalPreferences(): Flow<PersonalPreferences>
    suspend fun setQuickCapture(target: QuickCaptureTarget)
    suspend fun setShowPrompts(enabled: Boolean)
    suspend fun setAutoTagFromMood(enabled: Boolean)

    fun accessibilityPreferences(): Flow<AccessibilityPreferences>
    suspend fun setFontScale(scale: Float)
    suspend fun setReduceMotion(enabled: Boolean)
    suspend fun setHighContrast(enabled: Boolean)
}
