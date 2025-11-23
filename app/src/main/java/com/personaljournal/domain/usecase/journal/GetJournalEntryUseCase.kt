package com.personaljournal.domain.usecase.journal

import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.repository.JournalRepository
import javax.inject.Inject

class GetJournalEntryUseCase @Inject constructor(
    private val journalRepository: JournalRepository
) {
    suspend operator fun invoke(id: Long): JournalEntry? = journalRepository.getEntry(id)
}
