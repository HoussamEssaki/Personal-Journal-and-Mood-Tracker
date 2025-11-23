package com.personaljournal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaljournal.domain.model.ReminderSchedule
import com.personaljournal.domain.model.ReminderType
import com.personaljournal.domain.repository.ThemeMode
import com.personaljournal.domain.usecase.reminder.ManageReminderUseCase
import com.personaljournal.domain.usecase.security.SavePinUseCase
import com.personaljournal.domain.usecase.security.ToggleBiometricsUseCase
import com.personaljournal.domain.usecase.settings.UpdateLanguageUseCase
import com.personaljournal.domain.usecase.settings.UpdateThemeUseCase
import com.personaljournal.infrastructure.storage.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: String = "en",
    val reminder: ReminderSchedule? = null,
    val backupFile: File? = null,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val updateLanguageUseCase: UpdateLanguageUseCase,
    private val manageReminderUseCase: ManageReminderUseCase,
    private val savePinUseCase: SavePinUseCase,
    private val toggleBiometricsUseCase: ToggleBiometricsUseCase,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state

    init {
        viewModelScope.launch {
            updateThemeUseCase.observe().collect { mode ->
                _state.value = _state.value.copy(themeMode = mode)
            }
        }
        viewModelScope.launch {
            updateLanguageUseCase.observe().collect { language ->
                _state.value = _state.value.copy(language = language)
            }
        }
        viewModelScope.launch {
            manageReminderUseCase.observe().collect { reminder ->
                _state.value = _state.value.copy(reminder = reminder)
            }
        }
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { updateThemeUseCase.setTheme(mode) }
    }

    fun setLanguage(tag: String) {
        viewModelScope.launch { updateLanguageUseCase.set(tag) }
    }

    fun scheduleReminder(type: ReminderType, hour: Int, minute: Int) {
        viewModelScope.launch {
            manageReminderUseCase.schedule(
                ReminderSchedule(true, type, hour, minute)
            )
        }
    }

    fun disableReminder() {
        viewModelScope.launch { manageReminderUseCase.cancel() }
    }

    fun savePin(pin: String) {
        viewModelScope.launch { savePinUseCase(pin) }
    }

    fun toggleBiometrics(enabled: Boolean) {
        viewModelScope.launch { toggleBiometricsUseCase.setEnabled(enabled) }
    }

    fun backup() {
        viewModelScope.launch {
            val file = backupManager.exportDatabase()
            _state.value = _state.value.copy(backupFile = file, message = "Backup saved")
        }
    }
}
