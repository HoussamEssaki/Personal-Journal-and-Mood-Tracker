package com.personaljournal.presentation.viewmodel

import com.personaljournal.domain.model.EntryFilter
import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.model.Mood
import com.personaljournal.domain.model.MoodLevel
import com.personaljournal.domain.repository.JournalRepository
import com.personaljournal.domain.usecase.journal.DeleteJournalEntryUseCase
import com.personaljournal.domain.usecase.journal.SearchEntriesUseCase
import com.personaljournal.domain.usecase.journal.UpdateJournalEntryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlinx.datetime.Clock
import org.junit.Rule
import com.personaljournal.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `togglePin flips isPinned and calls update use case`() = runTest {
        val entry = JournalEntry(
            id = 1,
            title = "A",
            content = "B",
            createdAt = Clock.System.now(),
            mood = Mood(id = 1, label = "Happy", level = MoodLevel.GOOD, emoji = ":)", colorHex = "#00FF00")
        )
        val entriesFlow = MutableStateFlow(listOf(entry))
        val repo = FakeJournalRepo(entriesFlow)
        val viewModel = JournalViewModel(
            searchEntriesUseCase = SearchEntriesUseCase(repo),
            deleteJournalEntryUseCase = DeleteJournalEntryUseCase(repo),
            updateJournalEntryUseCase = UpdateJournalEntryUseCase(repo)
        )

        viewModel.togglePin(entriesFlow.value.first())
        advanceUntilIdle()

        assertEquals(1, repo.updateCalls.size)
        val updated = repo.updateCalls.first()
        assertEquals(true, updated.isPinned)
    }

    private class FakeJournalRepo(
        private val flow: MutableStateFlow<List<JournalEntry>>
    ) : JournalRepository {
        val updateCalls = mutableListOf<JournalEntry>()
        override fun observeEntries() = flow
        override fun searchEntries(filter: EntryFilter) = flow
        override suspend fun getEntry(id: Long) = flow.value.firstOrNull { it.id == id }
        override suspend fun createEntry(entry: JournalEntry) = 0L
        override suspend fun updateEntry(entry: JournalEntry) {
            updateCalls.add(entry)
        }
        override suspend fun deleteEntry(id: Long) = Unit
        override suspend fun upsertAll(entries: List<JournalEntry>) = Unit
        override suspend fun export(request: com.personaljournal.domain.model.ExportRequest) =
            Result.success(kotlin.io.path.createTempFile().toFile())
        override suspend fun syncPendingEntries() = Unit
        override suspend fun setPinned(id: Long, pinned: Boolean) = Unit
    }
}
