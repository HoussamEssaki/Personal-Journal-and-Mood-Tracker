package com.personaljournal.domain.usecase.settings

import com.personaljournal.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateAccentColorUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    fun observe(): Flow<String> = settingsRepository.accentColor()
    suspend fun set(colorHex: String) = settingsRepository.setAccentColor(colorHex)
}
