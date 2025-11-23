package com.personaljournal.di

import android.content.Context
import androidx.room.Room
import com.personaljournal.data.local.room.PersonalJournalDatabase
import com.personaljournal.data.local.room.dao.GoalsDao
import com.personaljournal.data.local.room.dao.JournalEntryDao
import com.personaljournal.data.local.room.dao.MediaAttachmentDao
import com.personaljournal.data.local.room.dao.MoodDao
import com.personaljournal.data.local.room.dao.NotificationLogDao
import com.personaljournal.data.local.room.dao.TagDao
import com.personaljournal.infrastructure.security.SecurePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import com.personaljournal.data.local.room.MIGRATION_1_2
import com.personaljournal.data.local.room.MIGRATION_2_3
import com.personaljournal.data.local.room.MIGRATION_3_4

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        securePreferences: SecurePreferences
    ): PersonalJournalDatabase {
        val passphraseKey = "db_passphrase"
        fun newPassphrase(): String = java.util.UUID.randomUUID().toString()
        fun buildWith(pass: String): PersonalJournalDatabase {
            val factory = SupportFactory(SQLiteDatabase.getBytes(pass.toCharArray()))
            return Room.databaseBuilder(
                context,
                PersonalJournalDatabase::class.java,
                "personal_journal.db"
            )
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
        }
        val cached = securePreferences.getString(passphraseKey)
        var passphrase = if (cached.isNotEmpty()) cached else newPassphrase().also {
            securePreferences.putString(passphraseKey, it)
        }
        return try {
            buildWith(passphrase)
        } catch (ex: Exception) {
            // Corrupted or legacy (unencrypted) DB; reset to recover instead of crashing
            listOf(
                "personal_journal.db",
                "personal_journal.db-wal",
                "personal_journal.db-shm"
            ).forEach { name -> context.deleteDatabase(name) }
            passphrase = newPassphrase().also { securePreferences.putString(passphraseKey, it) }
            buildWith(passphrase)
        }
    }

    @Provides fun provideEntryDao(db: PersonalJournalDatabase): JournalEntryDao = db.journalEntryDao()
    @Provides fun provideMediaDao(db: PersonalJournalDatabase): MediaAttachmentDao = db.mediaAttachmentDao()
    @Provides fun provideMoodDao(db: PersonalJournalDatabase): MoodDao = db.moodDao()
    @Provides fun provideTagDao(db: PersonalJournalDatabase): TagDao = db.tagDao()
    @Provides fun provideGoalsDao(db: PersonalJournalDatabase): GoalsDao = db.goalsDao()
    @Provides fun provideNotificationLogDao(db: PersonalJournalDatabase): NotificationLogDao = db.notificationLogDao()
}
