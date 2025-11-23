package com.personaljournal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaljournal.domain.model.ExportRange
import com.personaljournal.domain.model.StatsSnapshot
import com.personaljournal.domain.usecase.analytics.GenerateStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

data class TrendsUiState(
    val snapshot: StatsSnapshot = StatsSnapshot(),
    val selectedWindow: TrendWindow = TrendWindow.MONTH,
    val isLoading: Boolean = true,
    val error: String? = null
)

enum class TrendWindow(val days: Int, val label: String) {
    WEEK(7, "Week"),
    MONTH(30, "Month"),
    YEAR(365, "Year")
}

@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val generateStatsUseCase: GenerateStatsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TrendsUiState())
    val state: StateFlow<TrendsUiState> = _state

    init {
        refresh(TrendWindow.MONTH)
    }

    fun selectWindow(window: TrendWindow) {
        if (_state.value.selectedWindow == window && !_state.value.isLoading) return
        refresh(window)
    }

    private fun refresh(window: TrendWindow) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, selectedWindow = window, error = null)
            runCatching {
                generateStatsUseCase.compute(rangeFor(window))
            }.onSuccess { snapshot ->
                _state.value = _state.value.copy(
                    snapshot = snapshot,
                    isLoading = false,
                    error = null
                )
            }.onFailure { throwable ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = throwable.message ?: "Unable to load insights"
                )
            }
        }
    }

    private fun rangeFor(window: TrendWindow): ExportRange {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val start = today.minus(window.days - 1, DateTimeUnit.DAY)
        return ExportRange.Custom(start, today)
    }
}
