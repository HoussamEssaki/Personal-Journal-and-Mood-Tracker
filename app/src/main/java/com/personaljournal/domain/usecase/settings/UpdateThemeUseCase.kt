package com.personaljournal.domain.usecase.settings

import com.personaljournal.domain.repository.SettingsRepository
import com.personaljournal.domain.repository.ThemeMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateThemeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend fun setTheme(mode: ThemeMode) = settingsRepository.setTheme(mode)
    fun observe(): Flow<ThemeMode> = settingsRepository.themeMode()
}
