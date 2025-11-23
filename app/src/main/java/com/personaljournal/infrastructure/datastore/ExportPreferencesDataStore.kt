package com.personaljournal.infrastructure.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.personaljournal.domain.model.ExportFormat
import com.personaljournal.domain.repository.ExportPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.exportPreferencesDataStore by preferencesDataStore("export_preferences")

@Singleton
class ExportPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val data: Flow<ExportPreferences> = context.exportPreferencesDataStore.data.map { prefs ->
        ExportPreferences(
            format = prefs[KEY_FORMAT]?.let { runCatching { ExportFormat.valueOf(it) }.getOrNull() }
                ?: ExportFormat.PDF,
            includeMedia = prefs[KEY_INCLUDE_MEDIA] ?: true
        )
    }

    suspend fun setFormat(format: ExportFormat) {
        context.exportPreferencesDataStore.edit { prefs ->
            prefs[KEY_FORMAT] = format.name
        }
    }

    suspend fun setIncludeMedia(includeMedia: Boolean) {
        context.exportPreferencesDataStore.edit { prefs ->
            prefs[KEY_INCLUDE_MEDIA] = includeMedia
        }
    }

    companion object {
        private val KEY_FORMAT = stringPreferencesKey("export_format")
        private val KEY_INCLUDE_MEDIA = booleanPreferencesKey("export_include_media")
    }
}
