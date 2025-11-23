package com.personaljournal.domain.usecase.journal

import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.model.Mood
import com.personaljournal.domain.model.MoodLevel
import com.personaljournal.domain.repository.AnalyticsRepository
import com.personaljournal.domain.repository.JournalRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateJournalEntryUseCaseTest {

    private val journalRepository = mockk<JournalRepository>(relaxed = true)
    private val analyticsRepository = mockk<AnalyticsRepository>(relaxed = true)
    private val useCase = CreateJournalEntryUseCase(journalRepository, analyticsRepository)

    @Test
    fun `creates entry and tracks analytics`() = runTest {
        val entry = JournalEntry(
            id = 0,
            title = "Test",
            content = "Content",
            createdAt = Clock.System.now(),
            mood = Mood(1, "Happy", MoodLevel.GOOD, "😊", "#7ED321")
        )
        coEvery { journalRepository.createEntry(any()) } returns 1L

        useCase(entry)

        coVerify { journalRepository.createEntry(match { it.title == "Test" }) }
        coVerify { analyticsRepository.trackEntryCreated() }
    }
}
