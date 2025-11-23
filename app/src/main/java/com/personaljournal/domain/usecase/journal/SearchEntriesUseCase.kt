package com.personaljournal.domain.usecase.journal

import com.personaljournal.domain.model.EntryFilter
import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchEntriesUseCase @Inject constructor(
    private val journalRepository: JournalRepository
) {
    operator fun invoke(filter: EntryFilter): Flow<List<JournalEntry>> =
        journalRepository.searchEntries(filter)
}
