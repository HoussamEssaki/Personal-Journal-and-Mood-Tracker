package com.personaljournal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaljournal.domain.model.AccessibilityPreferences
import com.personaljournal.domain.model.DensityMode
import com.personaljournal.domain.model.LayoutPreferences
import com.personaljournal.domain.model.LayoutPreset
import com.personaljournal.domain.model.PersonalPreferences
import com.personaljournal.domain.model.QuickCaptureTarget
import com.personaljournal.domain.repository.DEFAULT_ACCENT_HEX
import com.personaljournal.domain.repository.ThemeMode
import com.personaljournal.domain.usecase.settings.PersonalizationSettingsUseCase
import com.personaljournal.domain.usecase.settings.UpdateAccentColorUseCase
import com.personaljournal.domain.usecase.settings.UpdateThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PersonalizationUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentHex: String = DEFAULT_ACCENT_HEX,
    val layout: LayoutPreferences = LayoutPreferences(),
    val personal: PersonalPreferences = PersonalPreferences(),
    val accessibility: AccessibilityPreferences = AccessibilityPreferences()
)

@HiltViewModel
class PersonalizationViewModel @Inject constructor(
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val updateAccentColorUseCase: UpdateAccentColorUseCase,
    private val personalizationSettingsUseCase: PersonalizationSettingsUseCase
) : ViewModel() {

    private val selectedThemeFlow = updateThemeUseCase.observe()
    private val accentFlow = updateAccentColorUseCase.observe()
    private val layoutFlow = personalizationSettingsUseCase.layout()
    private val personalFlow = personalizationSettingsUseCase.personal()
    private val accessibilityFlow = personalizationSettingsUseCase.accessibility()

    val state: StateFlow<PersonalizationUiState> = combine(
        selectedThemeFlow,
        accentFlow,
        layoutFlow,
        personalFlow,
        accessibilityFlow
    ) { themeMode, accent, layout, personal, accessibility ->
        PersonalizationUiState(
            themeMode = themeMode,
            accentHex = accent,
            layout = layout,
            personal = personal,
            accessibility = accessibility
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PersonalizationUiState())

    fun selectTheme(mode: ThemeMode) {
        viewModelScope.launch { updateThemeUseCase.setTheme(mode) }
    }

    fun selectAccent(colorHex: String) {
        viewModelScope.launch { updateAccentColorUseCase.set(colorHex) }
    }

    fun selectLayoutPreset(preset: LayoutPreset) {
        viewModelScope.launch { personalizationSettingsUseCase.setLayoutPreset(preset) }
    }

    fun selectDensityMode(mode: DensityMode) {
        viewModelScope.launch { personalizationSettingsUseCase.setDensityMode(mode) }
    }

    fun selectQuickCapture(target: QuickCaptureTarget) {
        viewModelScope.launch { personalizationSettingsUseCase.setQuickCapture(target) }
    }

    fun setShowPrompts(enabled: Boolean) {
        viewModelScope.launch { personalizationSettingsUseCase.setShowPrompts(enabled) }
    }

    fun setAutoTagFromMood(enabled: Boolean) {
        viewModelScope.launch { personalizationSettingsUseCase.setAutoTagFromMood(enabled) }
    }

    fun updateFontScale(scale: Float) {
        viewModelScope.launch { personalizationSettingsUseCase.setFontScale(scale) }
    }

    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch { personalizationSettingsUseCase.setReduceMotion(enabled) }
    }

    fun setHighContrast(enabled: Boolean) {
        viewModelScope.launch { personalizationSettingsUseCase.setHighContrast(enabled) }
    }
}
