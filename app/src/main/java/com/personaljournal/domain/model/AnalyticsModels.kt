package com.personaljournal.domain.model

import kotlinx.datetime.LocalDate

data class StatsSnapshot(
    val totalEntries: Int = 0,
    val activeStreakDays: Int = 0,
    val averageMoodLevel: Double = 0.0,
    val mostCommonMood: MoodLevel = MoodLevel.NEUTRAL,
    val promptsCompleted: Int = 0,
    val correlations: List<CorrelationInsight> = emptyList(),
    val recentTrend: List<MoodTrendPoint> = emptyList()
)

data class ReminderSchedule(
    val enabled: Boolean,
    val type: ReminderType,
    val hourOfDay: Int,
    val minute: Int,
    val dayOfWeek: Int? = null,
    val motivationalMessage: String? = null
)

enum class ReminderType { DAILY, WEEKLY }

data class ExportRequest(
    val range: ExportRange,
    val includeMedia: Boolean,
    val format: ExportFormat
)

sealed interface ExportRange {
    data object Last30Days : ExportRange
    data class Custom(val start: LocalDate, val end: LocalDate) : ExportRange
}

enum class ExportFormat { PDF, CSV, JSON }
