package com.personaljournal.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val message: String,
    val status: String,
    val timestamp: Long
)
