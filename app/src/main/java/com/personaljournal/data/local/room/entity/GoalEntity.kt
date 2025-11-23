package com.personaljournal.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val current: Int,
    val target: Int,
    val streakDays: Int,
    val unitLabel: String
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val label: String,
    val completion: List<Boolean>,
    val completionRate: Int
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val label: String,
    val unlocked: Boolean
)
