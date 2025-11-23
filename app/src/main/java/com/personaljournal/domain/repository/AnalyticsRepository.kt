package com.personaljournal.domain.repository

import com.personaljournal.domain.model.ExportRange
import com.personaljournal.domain.model.StatsSnapshot
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun observeStats(): Flow<StatsSnapshot>
    suspend fun compute(range: ExportRange): StatsSnapshot
    suspend fun trackEntryCreated()
}
