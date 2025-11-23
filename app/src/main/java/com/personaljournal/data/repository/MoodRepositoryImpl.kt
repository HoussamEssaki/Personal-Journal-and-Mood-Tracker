package com.personaljournal.data.repository

import com.personaljournal.data.local.mappers.toDomain
import com.personaljournal.data.local.mappers.toEntity
import com.personaljournal.data.local.room.dao.MoodDao
import com.personaljournal.domain.model.Mood
import com.personaljournal.domain.model.MoodLevel
import com.personaljournal.domain.repository.MoodRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class MoodRepositoryImpl @Inject constructor(
    private val moodDao: MoodDao
) : MoodRepository {

    override fun observeMoods(): Flow<List<Mood>> = moodDao.observe().map { moods ->
        moods.map { it.toDomain() }
    }

    override suspend fun getAll(): List<Mood> =
        moodDao.snapshot().map { it.toDomain() }

    override suspend fun getMoodByLevel(level: String): Mood? =
        moodDao.getByLevel(level)?.toDomain()

    override suspend fun seedDefaults() {
        val defaultMoods = listOf(
            Mood(1, "Excellent", MoodLevel.EXCELLENT, "😄", "#F5A623"),
            Mood(2, "Good", MoodLevel.GOOD, "😊", "#7ED321"),
            Mood(3, "Neutral", MoodLevel.NEUTRAL, "😐", "#9B9B9B"),
            Mood(4, "Bad", MoodLevel.POOR, "😟", "#F15A29"),
            Mood(5, "Terrible", MoodLevel.TERRIBLE, "😢", "#D0021B")
        )
        moodDao.insertAll(defaultMoods.map { it.toEntity() })
    }
}
