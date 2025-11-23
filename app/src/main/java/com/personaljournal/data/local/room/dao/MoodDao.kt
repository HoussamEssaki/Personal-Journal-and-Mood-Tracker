package com.personaljournal.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.personaljournal.data.local.room.entity.MoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Query("SELECT * FROM moods ORDER BY id ASC")
    fun observe(): Flow<List<MoodEntity>>

    @Query("SELECT * FROM moods ORDER BY id ASC")
    suspend fun snapshot(): List<MoodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(moods: List<MoodEntity>)

    @Query("SELECT * FROM moods WHERE level = :level LIMIT 1")
    suspend fun getByLevel(level: String): MoodEntity?
}
