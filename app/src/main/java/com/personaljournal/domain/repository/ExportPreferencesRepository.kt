package com.personaljournal.domain.repository

import com.personaljournal.domain.model.ExportFormat
import kotlinx.coroutines.flow.Flow

data class ExportPreferences(
    val format: ExportFormat,
    val includeMedia: Boolean
)

interface ExportPreferencesRepository {
    val preferences: Flow<ExportPreferences>
    suspend fun setFormat(format: ExportFormat)
    suspend fun setIncludeMedia(includeMedia: Boolean)
}
