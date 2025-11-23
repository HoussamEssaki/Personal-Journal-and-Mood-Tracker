package com.personaljournal.domain.usecase.security

import com.personaljournal.domain.repository.SecurityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ToggleBiometricsUseCase @Inject constructor(
    private val securityRepository: SecurityRepository
) {
    suspend fun setEnabled(enabled: Boolean) = securityRepository.enableBiometrics(enabled)
    fun observe(): Flow<Boolean> = securityRepository.biometricsEnabled()
}
