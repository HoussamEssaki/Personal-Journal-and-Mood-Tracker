package com.personaljournal.domain.repository

import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    suspend fun savePin(pin: String)
    suspend fun validatePin(pin: String): Boolean
    suspend fun clearPin()
    suspend fun unlock()
    fun observeLockState(): Flow<Boolean>
    suspend fun setAutoLockMinutes(minutes: Int)
    suspend fun shouldLock(): Boolean
    suspend fun enableBiometrics(enabled: Boolean)
    fun biometricsEnabled(): Flow<Boolean>
}
