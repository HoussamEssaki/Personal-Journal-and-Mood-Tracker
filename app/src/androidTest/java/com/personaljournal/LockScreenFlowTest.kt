package com.personaljournal

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Placeholder instrumentation test for lock screen PIN/biometric flow.
 * Marked @Ignore until a device/emulator script is wired.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class LockScreenFlowTest {

    @Ignore("Requires device/emulator and UI script")
    @Test
    fun pinAndBiometricFlow_smoke() {
        // TODO: Use Compose UI test or Espresso to launch LockScreen and verify:
        // - entering wrong PIN shows error
        // - forgot PIN path clears stored PIN and relocks
        // - biometric unavailable shows error snackbar
    }
}
