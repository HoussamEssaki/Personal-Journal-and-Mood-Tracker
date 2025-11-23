package com.personaljournal.domain.usecase.security

import com.personaljournal.domain.repository.SecurityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLockStateUseCase @Inject constructor(
    private val securityRepository: SecurityRepository
) {
    operator fun invoke(): Flow<Boolean> = securityRepository.observeLockState()
}
