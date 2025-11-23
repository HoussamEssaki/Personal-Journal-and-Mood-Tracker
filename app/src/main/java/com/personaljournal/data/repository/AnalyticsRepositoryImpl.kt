package com.personaljournal.data.repository

import com.personaljournal.data.local.room.dao.JournalEntryDao
import com.personaljournal.data.local.mappers.toDomain
import com.personaljournal.domain.model.CorrelationInsight
import com.personaljournal.domain.model.ExportRange
import com.personaljournal.domain.model.MoodLevel
import com.personaljournal.domain.model.StatsSnapshot
import com.personaljournal.domain.model.TrendDirection
import com.personaljournal.domain.repository.AnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.Instant.Companion.fromEpochMilliseconds
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlinx.datetime.LocalDate

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val journalEntryDao: JournalEntryDao
) : AnalyticsRepository {

    override fun observeStats(): Flow<StatsSnapshot> =
        journalEntryDao.observeEntries().map { entries ->
            val domainEntries = entries.map { it.toDomain() }
            buildStats(domainEntries)
        }

    override suspend fun compute(range: ExportRange): StatsSnapshot {
        val entries = journalEntryDao.snapshot().map { it.toDomain() }
        val filtered = when (range) {
            ExportRange.Last30Days -> {
                val threshold = Instant.fromEpochMilliseconds(System.currentTimeMillis())
                    .minus(30, DateTimeUnit.DAY, TimeZone.UTC)
                entries.filter { it.createdAt >= threshold }
            }
            is ExportRange.Custom -> {
                val start = range.start.atStartOfDayIn(TimeZone.UTC)
                val end = range.end.atStartOfDayIn(TimeZone.UTC)
                entries.filter { entry ->
                    entry.createdAt >= start && entry.createdAt <= end
                }
            }
        }
        return buildStats(filtered)
    }

    override suspend fun trackEntryCreated() {
        // Hook for analytics providers; left as no-op for offline-first mode.
    }

    private fun buildStats(entries: List<com.personaljournal.domain.model.JournalEntry>): StatsSnapshot {
        if (entries.isEmpty()) return StatsSnapshot()
        val average = entries.map { it.mood.level.score }.average()
        val grouped = entries.groupBy { it.mood.level }
        val mostCommon = grouped.maxByOrNull { it.value.size }?.key ?: MoodLevel.NEUTRAL
        val trend = buildDailyTrend(entries)
        val correlations = computeCorrelations(entries, average)
        val promptsCompleted = entries.count { it.prompt != null }
        return StatsSnapshot(
            totalEntries = entries.size,
            activeStreakDays = computeStreak(entries),
            averageMoodLevel = average,
            mostCommonMood = mostCommon,
            promptsCompleted = promptsCompleted,
            correlations = correlations,
            recentTrend = trend
        )
    }

    private fun computeStreak(entries: List<com.personaljournal.domain.model.JournalEntry>): Int {
        if (entries.isEmpty()) return 0
        val sorted = entries.sortedByDescending { it.createdAt }
        var streak = 0
        var lastDate = sorted.first().createdAt.toLocalDateTime(TimeZone.UTC).date
        for (entry in sorted) {
            val date = entry.createdAt.toLocalDateTime(TimeZone.UTC).date
            if (date == lastDate || date == lastDate.minus(1, DateTimeUnit.DAY)) {
                streak++
                lastDate = date
            } else {
                break
            }
        }
        return streak
    }

    private val MoodLevel.score: Int
        get() = when (this) {
            MoodLevel.EXCELLENT -> 5
            MoodLevel.GOOD -> 4
            MoodLevel.NEUTRAL -> 3
            MoodLevel.POOR -> 2
            MoodLevel.TERRIBLE -> 1
        }

    private fun computeCorrelations(
        entries: List<com.personaljournal.domain.model.JournalEntry>,
        overallAverage: Double
    ): List<CorrelationInsight> {
        if (entries.isEmpty()) return emptyList()
        val samples = entries.flatMap { entry ->
            entry.factors.map { factor -> factor.label to entry.mood.level.score.toDouble() }
        }
        if (samples.isEmpty()) return emptyList()

        return samples
            .groupBy { it.first }
            .map { (label, scores) ->
                val mean = scores.map { it.second }.average()
                val delta = mean - overallAverage
                val normalized = delta / 4.0 // rough scale so ±4 -> ±1
                CorrelationInsight(
                    factor = label,
                    correlation = normalized,
                    trend = when {
                        delta > 0.3 -> TrendDirection.UP
                        delta < -0.3 -> TrendDirection.DOWN
                        else -> TrendDirection.STABLE
                    }
                )
            }
            .sortedByDescending { abs(it.correlation) }
            .take(12)
    }

    /**
     * Aggregate mood per local calendar day so weekly/monthly/yearly views
     * have one representative score per day instead of one per entry.
     */
    private fun buildDailyTrend(
        entries: List<com.personaljournal.domain.model.JournalEntry>
    ): List<com.personaljournal.domain.model.MoodTrendPoint> {
        if (entries.isEmpty()) return emptyList()
        val tz = TimeZone.currentSystemDefault()
        val byDate: Map<LocalDate, List<com.personaljournal.domain.model.JournalEntry>> =
            entries.groupBy { it.createdAt.toLocalDateTime(tz).date }
        return byDate.entries
            .sortedBy { it.key }
            .map { (localDate, items) ->
                val avg = items.map { it.mood.level.score.toDouble() }.average()
                com.personaljournal.domain.model.MoodTrendPoint(
                    date = localDate.atStartOfDayIn(tz),
                    averageScore = avg
                )
            }
            .takeLast(365) // keep at most one year
    }
}
