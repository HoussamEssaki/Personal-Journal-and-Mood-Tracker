package com.personaljournal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaljournal.domain.usecase.security.ObserveLockStateUseCase
import com.personaljournal.domain.usecase.security.SavePinUseCase
import com.personaljournal.domain.usecase.security.ToggleBiometricsUseCase
import com.personaljournal.domain.usecase.security.UnlockJournalUseCase
import com.personaljournal.domain.usecase.security.ValidatePinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SecurityViewModel @Inject constructor(
    observeLockStateUseCase: ObserveLockStateUseCase,
    private val validatePinUseCase: ValidatePinUseCase,
    private val savePinUseCase: SavePinUseCase,
    private val toggleBiometricsUseCase: ToggleBiometricsUseCase,
    private val unlockJournalUseCase: UnlockJournalUseCase,
    private val securityRepository: com.personaljournal.domain.repository.SecurityRepository
) : ViewModel() {

    val isLocked: StateFlow<Boolean> = observeLockStateUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val biometricsEnabled: StateFlow<Boolean> = toggleBiometricsUseCase.observe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError

    fun unlock(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = validatePinUseCase(pin)
            if (!success) {
                _pinError.value = "Incorrect PIN. Try again."
            } else {
                _pinError.value = null
            }
            onResult(success)
        }
    }

    fun clearError() {
        _pinError.value = null
    }

    fun enableBiometrics(enabled: Boolean) {
        viewModelScope.launch { toggleBiometricsUseCase.setEnabled(enabled) }
    }

    fun savePin(pin: String) {
        viewModelScope.launch { savePinUseCase(pin) }
    }

    fun unlockWithBiometric() {
        viewModelScope.launch { unlockJournalUseCase() }
    }

    fun setError(message: String?) {
        _pinError.value = message
    }

    fun clearPinAndLock() {
        viewModelScope.launch {
            securityRepository.clearPin()
            _pinError.value = null
        }
    }
}
