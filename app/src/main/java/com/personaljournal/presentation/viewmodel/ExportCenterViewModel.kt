package com.personaljournal.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaljournal.domain.model.ExportFormat
import com.personaljournal.domain.repository.ExportPreferences
import com.personaljournal.domain.repository.ExportPreferencesRepository
import com.personaljournal.infrastructure.storage.BackupManager
import com.personaljournal.presentation.screens.export.ExportTab
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class ShareTargetUi(
    val id: String,
    val title: String,
    val description: String
)

data class BackupStatusUi(
    val lastBackup: String,
    val location: String,
    val lastRestore: String = "Never"
)

data class ExportCenterUiState(
    val selectedTab: ExportTab = ExportTab.Export,
    val selectedFormat: ExportFormat = ExportFormat.PDF,
    val includeMedia: Boolean = true,
    val shareTargets: List<ShareTargetUi> = defaultShareTargets(),
    val backupStatus: BackupStatusUi = BackupStatusUi("Never", "Device storage"),
    val statusMessage: String? = null,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false
)

@HiltViewModel
class ExportCenterViewModel @Inject constructor(
    private val preferencesRepository: ExportPreferencesRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _events = MutableSharedFlow<ExportCenterEvent>()
    val events: SharedFlow<ExportCenterEvent> = _events.asSharedFlow()

    private val _state = MutableStateFlow(ExportCenterUiState())
    val state: StateFlow<ExportCenterUiState> = _state

    init {
        viewModelScope.launch {
            preferencesRepository.preferences.collect { prefs ->
                applyPreferences(prefs)
            }
        }
    }

    fun selectTab(tab: ExportTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun selectFormat(format: ExportFormat) {
        viewModelScope.launch { preferencesRepository.setFormat(format) }
        _state.update { it.copy(selectedFormat = format) }
    }

    fun setIncludeMedia(include: Boolean) {
        viewModelScope.launch { preferencesRepository.setIncludeMedia(include) }
        _state.update { it.copy(includeMedia = include) }
    }

    fun shareWith(targetId: String) {
        val target = _state.value.shareTargets.firstOrNull { it.id == targetId }
        _state.update {
            it.copy(statusMessage = target?.let { tgt -> "Preparing share via ${tgt.title}..." })
        }
    }

    fun requestBackupDestination() {
        viewModelScope.launch {
            val filename = "personal-journal-backup-${Clock.System.now().epochSeconds}.zip"
            _events.emit(ExportCenterEvent.RequestBackupDestination(filename))
        }
    }

    fun backupTo(uri: Uri, locationLabel: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isBackingUp = true, statusMessage = "Saving backup...") }
            runCatching { backupManager.exportTo(uri) }
                .onSuccess {
                    val timestamp = formatTimestamp()
                    val destination = locationLabel ?: "Selected location"
                    _state.update { state ->
                        state.copy(
                            isBackingUp = false,
                            backupStatus = state.backupStatus.copy(
                                lastBackup = timestamp,
                                location = destination
                            ),
                            statusMessage = "Backup saved to $destination"
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isBackingUp = false,
                            statusMessage = "Backup failed: ${error.message ?: "Unknown error"}"
                        )
                    }
                }
        }
    }

    fun requestRestoreSource() {
        viewModelScope.launch { _events.emit(ExportCenterEvent.RequestRestoreSource) }
    }

    fun restoreFrom(uri: Uri, label: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isRestoring = true, statusMessage = "Restoring backup...") }
            runCatching { backupManager.importFrom(uri) }
                .onSuccess {
                    val timestamp = formatTimestamp()
                    val source = label ?: "selected file"
                    _state.update { state ->
                        state.copy(
                            isRestoring = false,
                            backupStatus = state.backupStatus.copy(lastRestore = timestamp),
                            statusMessage = "Restore complete from $source"
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isRestoring = false,
                            statusMessage = "Restore failed: ${error.message ?: "Unknown error"}"
                        )
                    }
                }
        }
    }

    fun dismissStatusMessage() {
        _state.update { it.copy(statusMessage = null) }
    }

    fun postStatus(message: String) {
        _state.update { it.copy(statusMessage = message) }
    }

    private fun applyPreferences(preferences: ExportPreferences) {
        _state.update {
            it.copy(
                selectedFormat = preferences.format,
                includeMedia = preferences.includeMedia
            )
        }
    }

    private fun formatTimestamp(): String {
        val local = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
        return "${local.date} at ${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
    }
}

private fun defaultShareTargets() = listOf(
    ShareTargetUi("email", "Email", "Send an export to yourself or a therapist."),
    ShareTargetUi("drive", "Cloud Drive", "Save to Drive/Files for cross-device access."),
    ShareTargetUi("print", "Print Ready", "Produce a printable PDF journal.")
)

sealed interface ExportCenterEvent {
    data class RequestBackupDestination(val suggestedName: String) : ExportCenterEvent
    data object RequestRestoreSource : ExportCenterEvent
}
