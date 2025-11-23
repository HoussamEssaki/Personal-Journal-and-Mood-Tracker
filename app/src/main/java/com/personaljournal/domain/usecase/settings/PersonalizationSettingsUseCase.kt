package com.personaljournal.domain.usecase.settings

import com.personaljournal.domain.model.AccessibilityPreferences
import com.personaljournal.domain.model.LayoutPreferences
import com.personaljournal.domain.model.LayoutPreset
import com.personaljournal.domain.model.PersonalPreferences
import com.personaljournal.domain.model.QuickCaptureTarget
import com.personaljournal.domain.model.DensityMode
import com.personaljournal.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class PersonalizationSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    fun layout(): Flow<LayoutPreferences> = settingsRepository.layoutPreferences()
    suspend fun setLayoutPreset(preset: LayoutPreset) = settingsRepository.setLayoutPreset(preset)
    suspend fun setDensityMode(mode: DensityMode) = settingsRepository.setDensityMode(mode)

    fun personal(): Flow<PersonalPreferences> = settingsRepository.personalPreferences()
    suspend fun setQuickCapture(target: QuickCaptureTarget) =
        settingsRepository.setQuickCapture(target)

    suspend fun setShowPrompts(enabled: Boolean) = settingsRepository.setShowPrompts(enabled)
    suspend fun setAutoTagFromMood(enabled: Boolean) =
        settingsRepository.setAutoTagFromMood(enabled)

    fun accessibility(): Flow<AccessibilityPreferences> =
        settingsRepository.accessibilityPreferences()

    suspend fun setFontScale(scale: Float) = settingsRepository.setFontScale(scale)
    suspend fun setReduceMotion(enabled: Boolean) = settingsRepository.setReduceMotion(enabled)
    suspend fun setHighContrast(enabled: Boolean) = settingsRepository.setHighContrast(enabled)
}
