package com.personaljournal.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.personaljournal.data.local.room.converters.RoomConverters
import com.personaljournal.data.local.room.dao.GoalsDao
import com.personaljournal.data.local.room.dao.JournalEntryDao
import com.personaljournal.data.local.room.dao.MediaAttachmentDao
import com.personaljournal.data.local.room.dao.MoodDao
import com.personaljournal.data.local.room.dao.NotificationLogDao
import com.personaljournal.data.local.room.dao.TagDao
import com.personaljournal.data.local.room.entity.AchievementEntity
import com.personaljournal.data.local.room.entity.EntryTagCrossRef
import com.personaljournal.data.local.room.entity.GoalEntity
import com.personaljournal.data.local.room.entity.HabitEntity
import com.personaljournal.data.local.room.entity.JournalEntryEntity
import com.personaljournal.data.local.room.entity.MediaAttachmentEntity
import com.personaljournal.data.local.room.entity.MoodEntity
import com.personaljournal.data.local.room.entity.NotificationLogEntity
import com.personaljournal.data.local.room.entity.TagEntity

@Database(
    entities = [
        JournalEntryEntity::class,
        MoodEntity::class,
        TagEntity::class,
        EntryTagCrossRef::class,
        MediaAttachmentEntity::class,
        GoalEntity::class,
        HabitEntity::class,
        AchievementEntity::class,
        NotificationLogEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class PersonalJournalDatabase : RoomDatabase() {
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun mediaAttachmentDao(): MediaAttachmentDao
    abstract fun moodDao(): MoodDao
    abstract fun tagDao(): TagDao
    abstract fun goalsDao(): GoalsDao
    abstract fun notificationLogDao(): NotificationLogDao
}
