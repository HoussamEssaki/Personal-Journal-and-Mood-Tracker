package com.personaljournal.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.personaljournal.data.local.room.entity.MediaAttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaAttachmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: MediaAttachmentEntity)

    @Query("DELETE FROM media_attachments WHERE attachmentId = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM media_attachments WHERE entryId = :entryId")
    fun observe(entryId: Long): Flow<List<MediaAttachmentEntity>>

    @Query("SELECT * FROM media_attachments WHERE entryId = :entryId")
    suspend fun getByEntry(entryId: Long): List<MediaAttachmentEntity>

    @Query("DELETE FROM media_attachments WHERE entryId = :entryId")
    suspend fun clearEntry(entryId: Long)

    @Query("SELECT * FROM media_attachments WHERE attachmentId = :id LIMIT 1")
    suspend fun getById(id: String): MediaAttachmentEntity?
}
