package com.personaljournal.domain.usecase.security

import com.personaljournal.domain.repository.SecurityRepository
import javax.inject.Inject

class SavePinUseCase @Inject constructor(
    private val securityRepository: SecurityRepository
) {
    suspend operator fun invoke(pin: String) {
        require(pin.length in 4..6) { "PIN must be 4-6 digits" }
        securityRepository.savePin(pin)
    }
}
