package com.personaljournal.infrastructure.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.personaljournal.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Singleton
class BiometricAuthenticator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun availability(): BiometricAvailability {
        val result = BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        return when (result) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                BiometricAvailability.Unavailable("No biometric hardware detected on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                BiometricAvailability.Unavailable("Biometric hardware is currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                BiometricAvailability.Unavailable("No fingerprint or face enrolled. Please enroll biometrics first.")
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                BiometricAvailability.Unavailable("Biometric authentication is not supported on this device.")
            else -> BiometricAvailability.Unavailable("Biometric authentication is not available.")
        }
    }

    suspend fun authenticate(activity: FragmentActivity): Boolean =
        suspendCancellableCoroutine { continuation ->
            val executor = ContextCompat.getMainExecutor(activity)
            val prompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        continuation.resume(true)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        continuation.resume(false)
                    }

                    override fun onAuthenticationFailed() {
                        continuation.resume(false)
                    }
                }
            )
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle(context.getString(R.string.lock_title))
                .setSubtitle(context.getString(R.string.biometric_prompt))
                .setNegativeButtonText(context.getString(android.R.string.cancel))
                .build()
            prompt.authenticate(info)
        }
}

sealed class BiometricAvailability {
    data object Available : BiometricAvailability()
    data class Unavailable(val reason: String) : BiometricAvailability()
}
