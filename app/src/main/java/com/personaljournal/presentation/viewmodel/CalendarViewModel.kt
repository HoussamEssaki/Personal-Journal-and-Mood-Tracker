package com.personaljournal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.model.MoodLevel
import com.personaljournal.domain.usecase.journal.ObserveJournalEntriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class CalendarUiState(
    val monthLabel: String = "",
    val weeks: List<List<CalendarDayUi>> = emptyList(),
    val selectedDate: LocalDate? = null,
    val selectedEntries: List<JournalEntry> = emptyList()
)

data class CalendarDayUi(
    val date: LocalDate?,
    val entries: List<JournalEntry>
) {
    val moodLevel: MoodLevel? = entries.firstOrNull()?.mood?.level
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    observeJournalEntriesUseCase: ObserveJournalEntriesUseCase
) : ViewModel() {

    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    private val timeZone = TimeZone.currentSystemDefault()

    private val currentMonth = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow(LocalDate.now())

    private val entryMapFlow = observeJournalEntriesUseCase()
        .map { entries ->
            entries.groupBy { entry ->
                val localDate = entry.createdAt.toLocalDateTime(timeZone).date
                LocalDate.of(localDate.year, localDate.monthNumber, localDate.dayOfMonth)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val state: StateFlow<CalendarUiState> = combine(
        entryMapFlow,
        currentMonth,
        selectedDate
    ) { entriesByDate, month, selected ->
        val adjustedSelection = when {
            selected.year == month.year && selected.month == month.month ->
                selected
            else -> month.atDay(1)
        }
        val weeks = buildWeeks(month, entriesByDate)
        CalendarUiState(
            monthLabel = month.format(monthFormatter),
            weeks = weeks,
            selectedDate = adjustedSelection,
            selectedEntries = entriesByDate[adjustedSelection].orEmpty().sortedByDescending { it.createdAt }
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CalendarUiState())

    fun onSelectDay(date: LocalDate) {
        viewModelScope.launch {
            selectedDate.emit(date)
        }
    }

    fun moveToPreviousMonth() {
        viewModelScope.launch {
            currentMonth.emit(currentMonth.value.minusMonths(1))
        }
    }

    fun moveToNextMonth() {
        viewModelScope.launch {
            currentMonth.emit(currentMonth.value.plusMonths(1))
        }
    }

    private fun buildWeeks(
        month: YearMonth,
        entries: Map<LocalDate, List<JournalEntry>>
    ): List<List<CalendarDayUi>> {
        val firstOfMonth = month.atDay(1)
        val startOffset = firstOfMonth.dayOfWeek.value % 7 // convert to Sunday = 0
        val daysInMonth = month.lengthOfMonth()

        val cells = mutableListOf<CalendarDayUi>()
        repeat(startOffset) { cells += CalendarDayUi(date = null, entries = emptyList()) }
        for (day in 1..daysInMonth) {
            val date = month.atDay(day)
            cells += CalendarDayUi(date = date, entries = entries[date].orEmpty())
        }
        while (cells.size % 7 != 0) {
            cells += CalendarDayUi(date = null, entries = emptyList())
        }
        return cells.chunked(7)
    }
}
