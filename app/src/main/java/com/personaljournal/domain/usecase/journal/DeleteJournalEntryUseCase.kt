package com.personaljournal.domain.usecase.journal

import com.personaljournal.domain.repository.JournalRepository
import javax.inject.Inject

class DeleteJournalEntryUseCase @Inject constructor(
    private val journalRepository: JournalRepository
) {
    suspend operator fun invoke(id: Long) {
        journalRepository.deleteEntry(id)
    }
}
