package com.personaljournal.domain.model

import kotlinx.datetime.Instant

data class Mood(
    val id: Long,
    val label: String,
    val level: MoodLevel,
    val emoji: String,
    val colorHex: String
)

enum class MoodLevel { EXCELLENT, GOOD, NEUTRAL, POOR, TERRIBLE }

data class MoodFactor(
    val id: String,
    val label: String,
    val category: FactorCategory,
    val importance: Int = 1
)

enum class FactorCategory { STRESSOR, SUPPORT, HABIT }

data class MoodTrendPoint(
    val date: Instant,
    val averageScore: Double
)

data class CorrelationInsight(
    val factor: String,
    val correlation: Double,
    val trend: TrendDirection
)

enum class TrendDirection { UP, DOWN, STABLE }
