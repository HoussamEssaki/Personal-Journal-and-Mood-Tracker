package com.personaljournal.data.repository

import com.personaljournal.data.local.room.dao.GoalsDao
import com.personaljournal.data.local.room.entity.AchievementEntity
import com.personaljournal.data.local.room.entity.GoalEntity
import com.personaljournal.data.local.room.entity.HabitEntity
import com.personaljournal.di.IoDispatcher
import com.personaljournal.domain.model.AchievementBadge
import com.personaljournal.domain.model.GoalProgress
import com.personaljournal.domain.model.HabitTracker
import com.personaljournal.domain.repository.GoalsRepository
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

@Singleton
class GoalsRepositoryImpl @Inject constructor(
    private val goalsDao: GoalsDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : GoalsRepository {

    private val seedScope = CoroutineScope(ioDispatcher)

    init {
        seedScope.launch { seedDefaultsIfNeeded() }
    }

    override fun observeGoals(): Flow<List<GoalProgress>> =
        goalsDao.observeGoals().map { list -> list.map { it.toDomain() } }

    override fun observeHabits(): Flow<List<HabitTracker>> =
        goalsDao.observeHabits().map { list -> list.map { it.toDomain() } }

    override fun observeAchievements(): Flow<List<AchievementBadge>> =
        goalsDao.observeAchievements().map { list -> list.map { it.toDomain() } }

    override suspend fun incrementGoal(goalId: String) = withContext(ioDispatcher) {
        val goal = goalsDao.getGoal(goalId) ?: return@withContext
        val newCurrent = min(goal.target, goal.current + 1)
        val updated = goal.copy(
            current = newCurrent,
            streakDays = if (newCurrent >= goal.target) goal.streakDays + 1 else goal.streakDays
        )
        goalsDao.upsertGoal(updated)
    }

    override suspend fun addGoal(title: String, target: Int, unitLabel: String) = withContext(ioDispatcher) {
        val safeTarget = target.coerceAtLeast(1)
        val entity = GoalEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            current = 0,
            target = safeTarget,
            streakDays = 0,
            unitLabel = unitLabel.ifBlank { "units" }
        )
        goalsDao.upsertGoal(entity)
    }

    override suspend fun addHabit(label: String) = withContext(ioDispatcher) {
        val entity = HabitEntity(
            id = UUID.randomUUID().toString(),
            label = label,
            completion = listOf(false, false, false, false, false, false, false),
            completionRate = 0
        )
        goalsDao.upsertHabit(entity)
    }

    override suspend fun toggleHabitCompletion(habitId: String, dayIndex: Int) = withContext(ioDispatcher) {
        val habit = goalsDao.getHabit(habitId) ?: return@withContext
        if (dayIndex !in habit.completion.indices) return@withContext
        val updatedDays = habit.completion.toMutableList().apply {
            this[dayIndex] = !this[dayIndex]
        }
        val rate = ((updatedDays.count { it }.toFloat() / updatedDays.size) * 100).toInt()
        goalsDao.upsertHabit(habit.copy(completion = updatedDays, completionRate = rate))
    }

    private suspend fun seedDefaultsIfNeeded() {
        if (goalsDao.countGoals() == 0) {
            goalsDao.upsertGoals(defaultGoals)
        }
        if (goalsDao.countHabits() == 0) {
            goalsDao.upsertHabits(defaultHabits)
        }
        if (goalsDao.countAchievements() == 0) {
            goalsDao.upsertAchievements(defaultAchievements)
        }
    }

    private fun GoalEntity.toDomain() = GoalProgress(
        id = id,
        title = title,
        current = current,
        target = target,
        streakDays = streakDays,
        unitLabel = unitLabel
    )

    private fun HabitEntity.toDomain() = HabitTracker(
        id = id,
        label = label,
        completion = completion,
        completionRate = completionRate
    )

    private fun AchievementEntity.toDomain() = AchievementBadge(
        id = id,
        label = label,
        unlocked = unlocked
    )

    companion object {
        private val defaultGoals = listOf(
            GoalEntity("read", "Read 10 pages daily", 7, 10, 5, "pages"),
            GoalEntity("meditate", "Meditate for 5 minutes", 5, 5, 12, "minutes"),
            GoalEntity("journal", "Weekly journal entry", 2, 3, 2, "entries")
        )

        private val defaultHabits = listOf(
            HabitEntity("walk", "Walk", listOf(true, true, true, false, true, true, false), 85),
            HabitEntity("exercise", "Exercise", listOf(true, false, true, true, false, true, false), 72),
            HabitEntity("journal_habit", "Journal", listOf(true, true, true, true, true, false, true), 92)
        )

        private val defaultAchievements = listOf(
            AchievementEntity("first_goal", "First Goal", true),
            AchievementEntity("streak7", "7-Day Streak", true),
            AchievementEntity("perfect_week", "Perfect Week", false),
            AchievementEntity("master", "Mindful Master", false),
            AchievementEntity("goal_setter", "Goal Setter", false)
        )
    }
}
