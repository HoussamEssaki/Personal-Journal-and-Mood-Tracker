package com.personaljournal.domain.repository

import com.personaljournal.domain.model.Mood
import kotlinx.coroutines.flow.Flow

interface MoodRepository {
    fun observeMoods(): Flow<List<Mood>>
    suspend fun getAll(): List<Mood>
    suspend fun getMoodByLevel(level: String): Mood?
    suspend fun seedDefaults()
}
