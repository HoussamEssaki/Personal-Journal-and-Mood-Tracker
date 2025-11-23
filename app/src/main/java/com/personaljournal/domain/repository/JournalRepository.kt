package com.personaljournal.domain.repository

import com.personaljournal.domain.model.EntryFilter
import com.personaljournal.domain.model.ExportRequest
import com.personaljournal.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow
import java.io.File

interface JournalRepository {
    fun observeEntries(): Flow<List<JournalEntry>>
    fun searchEntries(filter: EntryFilter): Flow<List<JournalEntry>>
    suspend fun getEntry(id: Long): JournalEntry?
    suspend fun createEntry(entry: JournalEntry): Long
    suspend fun updateEntry(entry: JournalEntry)
    suspend fun deleteEntry(id: Long)
    suspend fun upsertAll(entries: List<JournalEntry>)
    suspend fun export(request: ExportRequest): Result<File>
    suspend fun syncPendingEntries()
    suspend fun setPinned(id: Long, pinned: Boolean)
}
