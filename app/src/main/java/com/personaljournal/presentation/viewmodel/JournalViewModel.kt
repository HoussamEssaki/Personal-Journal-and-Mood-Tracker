package com.personaljournal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaljournal.domain.model.EntryFilter
import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.model.MoodLevel
import com.personaljournal.domain.usecase.journal.DeleteJournalEntryUseCase
import com.personaljournal.domain.usecase.journal.SearchEntriesUseCase
import com.personaljournal.domain.usecase.journal.UpdateJournalEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class JournalUiState(
    val entries: List<JournalEntry> = emptyList(),
    val filter: EntryFilter = EntryFilter(),
    val moodCalendar: Map<Int, MoodLevel> = emptyMap()
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val searchEntriesUseCase: SearchEntriesUseCase,
    private val deleteJournalEntryUseCase: DeleteJournalEntryUseCase,
    private val updateJournalEntryUseCase: UpdateJournalEntryUseCase
) : ViewModel() {

    private val filterFlow = MutableStateFlow(EntryFilter())

    val state: StateFlow<JournalUiState> = filterFlow
        .flatMapLatest { filter -> searchEntriesUseCase(filter) }
        .combine(filterFlow) { entries, filter ->
            JournalUiState(
                entries = entries,
                filter = filter,
                moodCalendar = entries.groupBy {
                    it.createdAt.toLocalDateTime(TimeZone.UTC).dayOfMonth
                }.mapValues { it.value.first().mood.level }
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, JournalUiState())

    fun updateFilter(filter: EntryFilter) {
        filterFlow.value = filter
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch { deleteJournalEntryUseCase(id) }
    }

    fun togglePin(entry: JournalEntry) {
        viewModelScope.launch {
            val updated = entry.copy(isPinned = !entry.isPinned)
            updateJournalEntryUseCase(updated)
        }
    }
}
