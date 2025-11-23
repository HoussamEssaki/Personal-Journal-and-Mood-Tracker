package com.personaljournal.domain.usecase.journal

import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.repository.AnalyticsRepository
import com.personaljournal.domain.repository.JournalRepository
import kotlinx.datetime.Clock
import javax.inject.Inject

class CreateJournalEntryUseCase @Inject constructor(
    private val journalRepository: JournalRepository,
    private val analyticsRepository: AnalyticsRepository
) {
    suspend operator fun invoke(entry: JournalEntry): Long {
        val enriched = entry.copy(
            createdAt = entry.createdAt,
            updatedAt = Clock.System.now()
        )
        val id = journalRepository.createEntry(enriched)
        analyticsRepository.trackEntryCreated()
        return id
    }
}
