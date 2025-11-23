package com.personaljournal.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.personaljournal.data.local.room.entity.AchievementEntity
import com.personaljournal.data.local.room.entity.GoalEntity
import com.personaljournal.data.local.room.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalsDao {
    @Query("SELECT * FROM goals ORDER BY title")
    fun observeGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM habits ORDER BY label")
    fun observeHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM achievements ORDER BY label")
    fun observeAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHabit(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAchievement(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoals(goals: List<GoalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHabits(habits: List<HabitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAchievements(achievements: List<AchievementEntity>)

    @Query("SELECT * FROM goals WHERE id = :goalId LIMIT 1")
    suspend fun getGoal(goalId: String): GoalEntity?

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    suspend fun getHabit(habitId: String): HabitEntity?

    @Query("SELECT COUNT(*) FROM goals")
    suspend fun countGoals(): Int

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun countHabits(): Int

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun countAchievements(): Int
}
