package com.personaljournal.domain.usecase.journal

import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.repository.JournalRepository
import kotlinx.datetime.Clock
import javax.inject.Inject

class UpdateJournalEntryUseCase @Inject constructor(
    private val journalRepository: JournalRepository
) {
    suspend operator fun invoke(entry: JournalEntry) {
        journalRepository.updateEntry(entry.copy(updatedAt = Clock.System.now()))
    }
}
