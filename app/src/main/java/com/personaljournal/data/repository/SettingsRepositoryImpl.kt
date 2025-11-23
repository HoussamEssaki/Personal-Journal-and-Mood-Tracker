package com.personaljournal.data.repository

import com.personaljournal.domain.model.AccessibilityPreferences
import com.personaljournal.domain.model.DensityMode
import com.personaljournal.domain.model.LayoutPreferences
import com.personaljournal.domain.model.LayoutPreset
import com.personaljournal.domain.model.PersonalPreferences
import com.personaljournal.domain.model.QuickCaptureTarget
import com.personaljournal.domain.repository.DEFAULT_ACCENT_HEX
import com.personaljournal.domain.repository.SettingsRepository
import com.personaljournal.domain.repository.ThemeMode
import com.personaljournal.infrastructure.security.SecurePreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val securePreferences: SecurePreferences
) : SettingsRepository {

    private val themeFlow =
        MutableStateFlow(securePreferences.getString(KEY_THEME).toThemeMode())
    private val languageFlow =
        MutableStateFlow(securePreferences.getString(KEY_LANGUAGE, "en"))
    private val accentFlow =
        MutableStateFlow(securePreferences.getString(KEY_ACCENT, DEFAULT_ACCENT_HEX))
    private val layoutFlow = MutableStateFlow(loadLayout())
    private val personalFlow = MutableStateFlow(loadPersonal())
    private val accessibilityFlow = MutableStateFlow(loadAccessibility())

    override fun themeMode(): Flow<ThemeMode> = themeFlow.asStateFlow()

    override suspend fun setTheme(mode: ThemeMode) {
        securePreferences.putString(KEY_THEME, mode.name)
        themeFlow.value = mode
    }

    override fun language(): Flow<String> = languageFlow.asStateFlow()

    override suspend fun setLanguage(languageTag: String) {
        securePreferences.putString(KEY_LANGUAGE, languageTag)
        languageFlow.value = languageTag
    }

    override fun accentColor(): Flow<String> = accentFlow.asStateFlow()

    override suspend fun setAccentColor(colorHex: String) {
        securePreferences.putString(KEY_ACCENT, colorHex)
        accentFlow.value = colorHex
    }

    override fun layoutPreferences(): Flow<LayoutPreferences> = layoutFlow.asStateFlow()

    override suspend fun setLayoutPreset(preset: LayoutPreset) {
        securePreferences.putString(KEY_LAYOUT_PRESET, preset.name)
        layoutFlow.value = layoutFlow.value.copy(preset = preset)
    }

    override suspend fun setDensityMode(mode: DensityMode) {
        securePreferences.putString(KEY_LAYOUT_DENSITY, mode.name)
        layoutFlow.value = layoutFlow.value.copy(densityMode = mode)
    }

    override fun personalPreferences(): Flow<PersonalPreferences> = personalFlow.asStateFlow()

    override suspend fun setQuickCapture(target: QuickCaptureTarget) {
        securePreferences.putString(KEY_QUICK_CAPTURE, target.name)
        personalFlow.value = personalFlow.value.copy(quickCapture = target)
    }

    override suspend fun setShowPrompts(enabled: Boolean) {
        securePreferences.putBoolean(KEY_SHOW_PROMPTS, enabled)
        personalFlow.value = personalFlow.value.copy(showPrompts = enabled)
    }

    override suspend fun setAutoTagFromMood(enabled: Boolean) {
        securePreferences.putBoolean(KEY_AUTO_TAG, enabled)
        personalFlow.value = personalFlow.value.copy(autoTagFromMood = enabled)
    }

    override fun accessibilityPreferences(): Flow<AccessibilityPreferences> =
        accessibilityFlow.asStateFlow()

    override suspend fun setFontScale(scale: Float) {
        val clamped = scale.coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE)
        securePreferences.putString(KEY_FONT_SCALE, clamped.toString())
        accessibilityFlow.value = accessibilityFlow.value.copy(fontScale = clamped)
    }

    override suspend fun setReduceMotion(enabled: Boolean) {
        securePreferences.putBoolean(KEY_REDUCE_MOTION, enabled)
        accessibilityFlow.value = accessibilityFlow.value.copy(reduceMotion = enabled)
    }

    override suspend fun setHighContrast(enabled: Boolean) {
        securePreferences.putBoolean(KEY_HIGH_CONTRAST, enabled)
        accessibilityFlow.value = accessibilityFlow.value.copy(highContrast = enabled)
    }

    private fun loadLayout(): LayoutPreferences =
        LayoutPreferences(
            preset = securePreferences
                .getString(KEY_LAYOUT_PRESET, LayoutPreset.BALANCED.name)
                .toLayoutPreset(),
            densityMode = securePreferences
                .getString(KEY_LAYOUT_DENSITY, DensityMode.COMFORTABLE.name)
                .toDensityMode()
        )

    private fun loadPersonal(): PersonalPreferences =
        PersonalPreferences(
            quickCapture = securePreferences
                .getString(KEY_QUICK_CAPTURE, QuickCaptureTarget.REFLECTION.name)
                .toQuickCapture(),
            showPrompts = securePreferences.getBoolean(KEY_SHOW_PROMPTS, true),
            autoTagFromMood = securePreferences.getBoolean(KEY_AUTO_TAG, true)
        )

    private fun loadAccessibility(): AccessibilityPreferences =
        AccessibilityPreferences(
            fontScale = securePreferences.getString(KEY_FONT_SCALE, "1.0").toFloatOrNull()
                ?.coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE) ?: 1f,
            reduceMotion = securePreferences.getBoolean(KEY_REDUCE_MOTION, false),
            highContrast = securePreferences.getBoolean(KEY_HIGH_CONTRAST, false)
        )

    private fun String.toThemeMode(): ThemeMode =
        runCatching { ThemeMode.valueOf(this) }.getOrDefault(ThemeMode.SYSTEM)

    private fun String.toLayoutPreset(): LayoutPreset =
        runCatching { LayoutPreset.valueOf(this) }.getOrDefault(LayoutPreset.BALANCED)

    private fun String.toDensityMode(): DensityMode =
        runCatching { DensityMode.valueOf(this) }.getOrDefault(DensityMode.COMFORTABLE)

    private fun String.toQuickCapture(): QuickCaptureTarget =
        runCatching { QuickCaptureTarget.valueOf(this) }.getOrDefault(QuickCaptureTarget.REFLECTION)

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_ACCENT = "accent_color"
        private const val KEY_LAYOUT_PRESET = "layout_preset"
        private const val KEY_LAYOUT_DENSITY = "layout_density"
        private const val KEY_QUICK_CAPTURE = "quick_capture_target"
        private const val KEY_SHOW_PROMPTS = "show_prompts"
        private const val KEY_AUTO_TAG = "auto_tag_from_mood"
        private const val KEY_FONT_SCALE = "font_scale"
        private const val KEY_REDUCE_MOTION = "reduce_motion"
        private const val KEY_HIGH_CONTRAST = "high_contrast"
        private const val MIN_FONT_SCALE = 0.9f
        private const val MAX_FONT_SCALE = 1.4f
    }
}
