package com.personaljournal.domain.usecase.security

import com.personaljournal.domain.repository.SecurityRepository
import javax.inject.Inject

class UnlockJournalUseCase @Inject constructor(
    private val securityRepository: SecurityRepository
) {
    suspend operator fun invoke() = securityRepository.unlock()
}
