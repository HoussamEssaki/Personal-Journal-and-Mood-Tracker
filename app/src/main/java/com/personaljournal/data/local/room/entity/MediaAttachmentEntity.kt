package com.personaljournal.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media_attachments",
    indices = [Index("entryId")]
)
data class MediaAttachmentEntity(
    @PrimaryKey val attachmentId: String,
    val entryId: Long,
    val type: String,
    val filePath: String,
    val thumbnailPath: String?,
    val durationSeconds: Int?,
    val createdAtEpoch: Long
)
