package com.personaljournal.data.repository

import com.personaljournal.domain.repository.SecurityRepository
import com.personaljournal.infrastructure.security.SecurePreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.security.MessageDigest

@Singleton
class SecurityRepositoryImpl @Inject constructor(
    private val securePreferences: SecurePreferences,
    @com.personaljournal.di.IoDispatcher private val dispatcher: CoroutineDispatcher
) : SecurityRepository {

    private val lockState = MutableStateFlow(true)
    private val biometrics = MutableStateFlow(securePreferences.getBoolean(KEY_BIOMETRICS))

    override suspend fun savePin(pin: String) = withContext(dispatcher) {
        securePreferences.putString(KEY_PIN_HASH, hash(pin))
        lockState.value = true
    }

    override suspend fun validatePin(pin: String): Boolean = withContext(dispatcher) {
        val stored = securePreferences.getString(KEY_PIN_HASH)
        if (stored.isEmpty()) {
            lockState.value = false
            return@withContext true
        }
        val isValid = stored == hash(pin)
        if (isValid) {
            securePreferences.putLong(KEY_LAST_UNLOCKED, System.currentTimeMillis())
            lockState.value = false
        } else {
            lockState.value = true
        }
        isValid
    }

    override suspend fun clearPin() = withContext(dispatcher) {
        securePreferences.putString(KEY_PIN_HASH, "")
        lockState.value = true
    }

    override suspend fun unlock() = withContext(dispatcher) {
        securePreferences.putLong(KEY_LAST_UNLOCKED, System.currentTimeMillis())
        lockState.value = false
    }

    override fun observeLockState(): Flow<Boolean> = lockState.asStateFlow()

    override suspend fun setAutoLockMinutes(minutes: Int) = withContext(dispatcher) {
        securePreferences.putLong(KEY_AUTO_LOCK_MINUTES, minutes.toLong())
    }

    override suspend fun shouldLock(): Boolean = withContext(dispatcher) {
        val lastUnlocked = securePreferences.getLong(KEY_LAST_UNLOCKED, 0)
        val minutes = securePreferences.getLong(KEY_AUTO_LOCK_MINUTES, 5)
        if (lastUnlocked == 0L) return@withContext true
        val elapsedMinutes = (System.currentTimeMillis() - lastUnlocked) / 60000
        val lockNeeded = elapsedMinutes >= minutes
        lockState.value = lockNeeded
        lockNeeded
    }

    override suspend fun enableBiometrics(enabled: Boolean) = withContext(dispatcher) {
        securePreferences.putBoolean(KEY_BIOMETRICS, enabled)
        biometrics.value = enabled
    }

    override fun biometricsEnabled(): Flow<Boolean> = biometrics.asStateFlow()

    private fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_LAST_UNLOCKED = "last_unlocked"
        private const val KEY_AUTO_LOCK_MINUTES = "auto_lock_minutes"
        private const val KEY_BIOMETRICS = "biometric_enabled"
    }
}
