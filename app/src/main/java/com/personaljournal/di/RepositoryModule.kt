package com.personaljournal.di

import com.personaljournal.data.remote.firebase.FirestoreJournalRemoteDataSource
import com.personaljournal.data.remote.firebase.JournalRemoteDataSource
import com.personaljournal.data.repository.AnalyticsRepositoryImpl
import com.personaljournal.data.repository.ExportPreferencesRepositoryImpl
import com.personaljournal.data.repository.GoalsRepositoryImpl
import com.personaljournal.data.repository.JournalRepositoryImpl
import com.personaljournal.data.repository.MediaRepositoryImpl
import com.personaljournal.data.repository.MoodRepositoryImpl
import com.personaljournal.data.repository.NotificationHistoryRepositoryImpl
import com.personaljournal.data.repository.ReminderRepositoryImpl
import com.personaljournal.data.repository.SecurityRepositoryImpl
import com.personaljournal.data.repository.SettingsRepositoryImpl
import com.personaljournal.data.repository.TagRepositoryImpl
import com.personaljournal.domain.repository.AnalyticsRepository
import com.personaljournal.domain.repository.ExportPreferencesRepository
import com.personaljournal.domain.repository.GoalsRepository
import com.personaljournal.domain.repository.JournalRepository
import com.personaljournal.domain.repository.MediaRepository
import com.personaljournal.domain.repository.MoodRepository
import com.personaljournal.domain.repository.NotificationHistoryRepository
import com.personaljournal.domain.repository.ReminderRepository
import com.personaljournal.domain.repository.SecurityRepository
import com.personaljournal.domain.repository.SettingsRepository
import com.personaljournal.domain.repository.TagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton abstract fun bindJournalRepository(impl: JournalRepositoryImpl): JournalRepository
    @Binds @Singleton abstract fun bindMoodRepository(impl: MoodRepositoryImpl): MoodRepository
    @Binds @Singleton abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository
    @Binds @Singleton abstract fun bindMediaRepository(impl: MediaRepositoryImpl): MediaRepository
    @Binds @Singleton abstract fun bindAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository
    @Binds @Singleton abstract fun bindSecurityRepository(impl: SecurityRepositoryImpl): SecurityRepository
    @Binds @Singleton abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
    @Binds @Singleton abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository
    @Binds @Singleton abstract fun bindExportPreferencesRepository(impl: ExportPreferencesRepositoryImpl): ExportPreferencesRepository
    @Binds @Singleton abstract fun bindNotificationHistoryRepository(impl: NotificationHistoryRepositoryImpl): NotificationHistoryRepository
    @Binds @Singleton abstract fun bindGoalsRepository(impl: GoalsRepositoryImpl): GoalsRepository
    @Binds @Singleton abstract fun bindRemoteDataSource(impl: FirestoreJournalRemoteDataSource): JournalRemoteDataSource
}
