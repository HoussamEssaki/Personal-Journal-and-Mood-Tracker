package com.personaljournal.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "journal_entries",
    indices = [
        Index("createdAtEpoch"),
        Index("moodId"),
        Index("tagsSearchable")
    ]
)
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val content: String,
    val richTextJson: String,
    val createdAtEpoch: Long,
    val updatedAtEpoch: Long,
    val moodId: Long,
    val moodLevel: String,
    val promptId: String?,
    val promptTitle: String?,
    val promptDescription: String?,
    val mediaPaths: List<String>,
    val tags: List<String>,
    val tagsSearchable: String,
    val secondaryEmotions: List<String>,
    val factors: List<String>,
    val isEncrypted: Boolean,
    val isSynced: Boolean,
    val locationLat: Double?,
    val locationLon: Double?,
    val locationName: String?,
    val weatherTemp: Double?,
    val weatherCondition: String?,
    val isPinned: Boolean = false
)
