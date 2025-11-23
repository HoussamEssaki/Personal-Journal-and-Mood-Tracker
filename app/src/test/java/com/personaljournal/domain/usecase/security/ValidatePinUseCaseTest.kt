package com.personaljournal.domain.usecase.security

import com.personaljournal.domain.repository.SecurityRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatePinUseCaseTest {

    private class FakeSecurityRepository : SecurityRepository {
        var receivedPin: String? = null
        override suspend fun savePin(pin: String) { receivedPin = pin }
        override suspend fun validatePin(pin: String): Boolean {
            receivedPin = pin
            return true
        }
        override suspend fun clearPin() {}
        override suspend fun unlock() {}
        override fun observeLockState() = throw UnsupportedOperationException()
        override suspend fun setAutoLockMinutes(minutes: Int) {}
        override suspend fun shouldLock(): Boolean = false
        override suspend fun enableBiometrics(enabled: Boolean) {}
        override fun biometricsEnabled() = throw UnsupportedOperationException()
    }

    @Test
    fun `rejects pins shorter than 4 or longer than 6`() = runBlocking {
        val repo = FakeSecurityRepository()
        val useCase = ValidatePinUseCase(repo)

        assertFalse(useCase("123"))
        assertFalse(useCase("1234567"))
        // Repository should not be called when length is invalid
        assertTrue(repo.receivedPin == null)
    }

    @Test
    fun `accepts pins of length between 4 and 6`() = runBlocking {
        val repo = FakeSecurityRepository()
        val useCase = ValidatePinUseCase(repo)

        assertTrue(useCase("1234"))
        assertTrue(useCase("123456"))
        assertTrue(repo.receivedPin == "123456")
    }
}
