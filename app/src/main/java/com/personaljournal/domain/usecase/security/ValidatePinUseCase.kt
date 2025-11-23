package com.personaljournal.domain.usecase.security

import com.personaljournal.domain.repository.SecurityRepository
import javax.inject.Inject

class ValidatePinUseCase @Inject constructor(
    private val securityRepository: SecurityRepository
) {
    suspend operator fun invoke(pin: String): Boolean {
        if (pin.length !in 4..6) return false
        return securityRepository.validatePin(pin)
    }
}
