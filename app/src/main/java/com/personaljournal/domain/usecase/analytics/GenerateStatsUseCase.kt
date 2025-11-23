package com.personaljournal.domain.usecase.analytics

import com.personaljournal.domain.model.ExportRange
import com.personaljournal.domain.model.StatsSnapshot
import com.personaljournal.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GenerateStatsUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) {
    fun observe(): Flow<StatsSnapshot> = analyticsRepository.observeStats()
    suspend fun compute(range: ExportRange): StatsSnapshot = analyticsRepository.compute(range)
}
