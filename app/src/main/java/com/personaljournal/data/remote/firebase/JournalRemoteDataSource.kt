package com.personaljournal.data.remote.firebase

import com.personaljournal.domain.model.JournalEntry

interface JournalRemoteDataSource {
    suspend fun pushPending(entries: List<JournalEntry>)
    suspend fun pullUpdates(): List<JournalEntry>
}
