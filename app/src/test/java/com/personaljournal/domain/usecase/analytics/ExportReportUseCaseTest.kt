package com.personaljournal.domain.usecase.analytics

import com.personaljournal.domain.model.ExportFormat
import com.personaljournal.domain.model.ExportRange
import com.personaljournal.domain.model.ExportRequest
import com.personaljournal.domain.repository.JournalRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExportReportUseCaseTest {

    @Test
    fun `delegates export to repository and returns file`() = runBlocking {
        val request = ExportRequest(
            format = ExportFormat.JSON,
            range = ExportRange.Last30Days,
            includeMedia = false
        )
        val repo = FakeJournalRepository()
        val useCase = ExportReportUseCase(repo)

        val result = useCase(request)

        assertTrue(result.isSuccess)
        assertEquals(request, repo.lastRequest)
        assertTrue(result.getOrThrow().exists())
    }

    private class FakeJournalRepository : JournalRepository {
        var lastRequest: ExportRequest? = null
        override suspend fun export(request: ExportRequest): Result<File> {
            lastRequest = request
            val file = kotlin.io.path.createTempFile(suffix = ".json").toFile()
            file.writeText("""{"ok":true}""")
            return Result.success(file)
        }

        // Unused for this test
        override fun observeEntries() = throw UnsupportedOperationException()
        override fun searchEntries(filter: com.personaljournal.domain.model.EntryFilter) = throw UnsupportedOperationException()
        override suspend fun getEntry(id: Long) = null
        override suspend fun createEntry(entry: com.personaljournal.domain.model.JournalEntry) = 0L
        override suspend fun updateEntry(entry: com.personaljournal.domain.model.JournalEntry) = Unit
        override suspend fun deleteEntry(id: Long) = Unit
        override suspend fun upsertAll(entries: List<com.personaljournal.domain.model.JournalEntry>) = Unit
        override suspend fun syncPendingEntries() = Unit
        override suspend fun setPinned(id: Long, pinned: Boolean) = Unit
    }
}
