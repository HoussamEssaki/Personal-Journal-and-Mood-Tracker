package com.personaljournal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaljournal.domain.model.ExportFormat
import com.personaljournal.domain.model.ExportRange
import com.personaljournal.domain.model.StatsSnapshot
import com.personaljournal.domain.usecase.analytics.ExportReportUseCase
import com.personaljournal.domain.usecase.analytics.GenerateStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val stats: StatsSnapshot = StatsSnapshot(),
    val exportFile: File? = null,
    val isExporting: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val generateStatsUseCase: GenerateStatsUseCase,
    private val exportReportUseCase: ExportReportUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsUiState())
    val state: StateFlow<AnalyticsUiState> = _state

    init {
        viewModelScope.launch {
            generateStatsUseCase.observe().collect { stats ->
                _state.value = _state.value.copy(stats = stats)
            }
        }
    }

    fun export(
        format: ExportFormat,
        includeMedia: Boolean,
        range: ExportRange = ExportRange.Last30Days
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true, error = null)
            val request = com.personaljournal.domain.model.ExportRequest(
                range = range,
                includeMedia = includeMedia,
                format = format
            )
            val result = exportReportUseCase(request)
            _state.value = result.fold(
                onSuccess = { file ->
                    _state.value.copy(isExporting = false, exportFile = file)
                },
                onFailure = { error ->
                    _state.value.copy(isExporting = false, error = error.message)
                }
            )
        }
    }

    fun clearExportStatus() {
        _state.value = _state.value.copy(exportFile = null, error = null)
    }
}
