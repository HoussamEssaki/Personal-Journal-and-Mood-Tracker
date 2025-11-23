package com.personaljournal.domain.repository

import com.personaljournal.domain.model.AchievementBadge
import com.personaljournal.domain.model.GoalProgress
import com.personaljournal.domain.model.HabitTracker
import kotlinx.coroutines.flow.Flow

interface GoalsRepository {
    fun observeGoals(): Flow<List<GoalProgress>>
    fun observeHabits(): Flow<List<HabitTracker>>
    fun observeAchievements(): Flow<List<AchievementBadge>>
    suspend fun incrementGoal(goalId: String)
    suspend fun toggleHabitCompletion(habitId: String, dayIndex: Int)
    suspend fun addGoal(title: String, target: Int, unitLabel: String)
    suspend fun addHabit(label: String)
}
