package com.personaljournal.data.repository

import com.personaljournal.domain.model.ExportFormat
import com.personaljournal.domain.repository.ExportPreferences
import com.personaljournal.domain.repository.ExportPreferencesRepository
import com.personaljournal.infrastructure.datastore.ExportPreferencesDataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ExportPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: ExportPreferencesDataStore
) : ExportPreferencesRepository {

    override val preferences: Flow<ExportPreferences> = dataStore.data

    override suspend fun setFormat(format: ExportFormat) = dataStore.setFormat(format)

    override suspend fun setIncludeMedia(includeMedia: Boolean) =
        dataStore.setIncludeMedia(includeMedia)
}
