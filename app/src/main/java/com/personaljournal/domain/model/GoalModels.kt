package com.personaljournal.domain.model

data class GoalProgress(
    val id: String,
    val title: String,
    val current: Int,
    val target: Int,
    val streakDays: Int,
    val unitLabel: String
) {
    val progressFraction: Float
        get() = if (target == 0) 0f else (current.toFloat() / target).coerceIn(0f, 1f)

    val statusLabel: String
        get() = "$current/$target $unitLabel"
}

data class HabitTracker(
    val id: String,
    val label: String,
    val completion: List<Boolean>, // size 7, Monday..Sunday
    val completionRate: Int
)

data class AchievementBadge(
    val id: String,
    val label: String,
    val unlocked: Boolean
)
