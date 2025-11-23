package com.personaljournal.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moods")
data class MoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val label: String,
    val level: String,
    val emoji: String,
    val colorHex: String
)
