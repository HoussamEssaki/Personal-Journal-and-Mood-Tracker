package com.personaljournal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaljournal.domain.model.AccessibilityPreferences
import com.personaljournal.domain.repository.DEFAULT_ACCENT_HEX
import com.personaljournal.domain.repository.ThemeMode
import com.personaljournal.domain.usecase.bootstrap.SeedBootstrapUseCase
import com.personaljournal.domain.usecase.journal.SyncEntriesUseCase
import com.personaljournal.domain.usecase.reminder.ManageReminderUseCase
import com.personaljournal.domain.usecase.settings.PersonalizationSettingsUseCase
import com.personaljournal.domain.usecase.settings.UpdateAccentColorUseCase
import com.personaljournal.domain.usecase.settings.UpdateLanguageUseCase
import com.personaljournal.domain.usecase.settings.UpdateThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val updateLanguageUseCase: UpdateLanguageUseCase,
    private val updateAccentColorUseCase: UpdateAccentColorUseCase,
    private val seedBootstrapUseCase: SeedBootstrapUseCase,
    private val syncEntriesUseCase: SyncEntriesUseCase,
    private val reminderUseCase: ManageReminderUseCase,
    personalizationSettingsUseCase: PersonalizationSettingsUseCase
) : ViewModel() {

    val theme: StateFlow<ThemeMode> = updateThemeUseCase.observe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    val language: StateFlow<String> = updateLanguageUseCase.observe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "en")

    val accentColor: StateFlow<String> = updateAccentColorUseCase.observe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, DEFAULT_ACCENT_HEX)

    val reminderSchedule = reminderUseCase.observe()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val accessibility: StateFlow<AccessibilityPreferences> =
        personalizationSettingsUseCase.accessibility()
            .stateIn(viewModelScope, SharingStarted.Eagerly, AccessibilityPreferences())

    init {
        viewModelScope.launch { seedBootstrapUseCase() }
        viewModelScope.launch { syncEntriesUseCase() }
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { updateThemeUseCase.setTheme(mode) }
    }

    fun setLanguage(tag: String) {
        viewModelScope.launch { updateLanguageUseCase.set(tag) }
    }

    fun setAccentColor(hex: String) {
        viewModelScope.launch { updateAccentColorUseCase.set(hex) }
    }
}
