package com.personaljournal.domain.usecase.analytics

import com.personaljournal.domain.model.ExportRange
import com.personaljournal.domain.model.MoodLevel
import com.personaljournal.domain.model.StatsSnapshot
import com.personaljournal.domain.repository.AnalyticsRepository
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that GenerateStatsUseCase computes over the requested period.
 */
class AnalyticsPeriodTest {

    private val now: Instant = Clock.System.now()

    @Test
    fun compute_last30Days_filters_outside_range() = runBlocking {
        val repo = FakeAnalyticsRepo(
            listOf(
                moodEntry(daysAgo = 10, level = MoodLevel.EXCELLENT),
                moodEntry(daysAgo = 20, level = MoodLevel.GOOD),
                moodEntry(daysAgo = 40, level = MoodLevel.TERRIBLE) // should be excluded
            )
        )
        val useCase = GenerateStatsUseCase(repo)

        val stats = useCase.compute(ExportRange.Last30Days)

        assertEquals(2, stats.totalEntries)
        assertTrue("Average should not include the 40-day sample", stats.averageMoodLevel > 3.0)
    }

    @Test
    fun compute_custom_range_bounds_inclusive() = runBlocking {
        val start = now.minus(7, DateTimeUnit.DAY, TimeZone.UTC)
        val end = now
        val repo = FakeAnalyticsRepo(
            listOf(
                moodEntry(daysAgo = 7, level = MoodLevel.GOOD),
                moodEntry(daysAgo = 1, level = MoodLevel.POOR),
                moodEntry(daysAgo = 10, level = MoodLevel.EXCELLENT) // outside range
            )
        )
        val useCase = GenerateStatsUseCase(repo)

        val stats = useCase.compute(
            ExportRange.Custom(
                start = start.toLocalDateTime(TimeZone.UTC).date,
                end = end.toLocalDateTime(TimeZone.UTC).date
            )
        )

        assertEquals(2, stats.totalEntries)
    }

    private fun moodEntry(daysAgo: Long, level: MoodLevel): StatsSnapshot {
        val instant = now.minus(daysAgo, DateTimeUnit.DAY, TimeZone.UTC)
        return StatsSnapshot(
            totalEntries = 1,
            averageMoodLevel = level.score.toDouble(),
            mostCommonMood = level,
            recentTrend = listOf(
                com.personaljournal.domain.model.MoodTrendPoint(
                    date = instant,
                    averageScore = level.score.toDouble()
                )
            )
        )
    }

    /**
        Lightweight fake repo returning canned snapshots; compute() selects based on ExportRange.
     */
    private class FakeAnalyticsRepo(
        private val snapshots: List<StatsSnapshot>
    ) : AnalyticsRepository {
        override fun observeStats() = throw UnsupportedOperationException()
        override suspend fun compute(range: ExportRange): StatsSnapshot {
            val cutoffStart = when (range) {
                ExportRange.Last30Days ->
                    Clock.System.now().minus(30, DateTimeUnit.DAY, TimeZone.UTC)
                is ExportRange.Custom ->
                    range.start.atStartOfDayIn(TimeZone.UTC)
            }
            val cutoffEnd = when (range) {
                ExportRange.Last30Days -> Clock.System.now()
                is ExportRange.Custom -> range.end.atStartOfDayIn(TimeZone.UTC)
            }

            val kept = snapshots.filter { snap ->
                snap.recentTrend.lastOrNull()?.date?.let { it >= cutoffStart && it <= cutoffEnd } == true
            }
            return kept.reduceOrNull { acc, snap ->
                acc.copy(
                    totalEntries = acc.totalEntries + snap.totalEntries,
                    averageMoodLevel = (acc.averageMoodLevel + snap.averageMoodLevel) / 2,
                    recentTrend = acc.recentTrend + snap.recentTrend
                )
            } ?: StatsSnapshot()
        }

        override suspend fun trackEntryCreated() = Unit
    }

    private val MoodLevel.score: Int
        get() = when (this) {
            MoodLevel.EXCELLENT -> 5
            MoodLevel.GOOD -> 4
            MoodLevel.NEUTRAL -> 3
            MoodLevel.POOR -> 2
            MoodLevel.TERRIBLE -> 1
        }
}
