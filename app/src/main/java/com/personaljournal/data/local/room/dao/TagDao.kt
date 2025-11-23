package com.personaljournal.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.personaljournal.data.local.room.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY label ASC")
    fun observe(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY label ASC")
    suspend fun snapshot(): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tag: TagEntity): Long

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM tags WHERE lower(label) IN (:labels)")
    suspend fun findByLabels(labels: List<String>): List<TagEntity>
}
