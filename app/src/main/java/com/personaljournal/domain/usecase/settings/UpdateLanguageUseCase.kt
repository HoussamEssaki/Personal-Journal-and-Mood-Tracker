package com.personaljournal.domain.usecase.settings

import com.personaljournal.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateLanguageUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun set(languageTag: String) = settingsRepository.setLanguage(languageTag)
    fun observe(): Flow<String> = settingsRepository.language()
}
