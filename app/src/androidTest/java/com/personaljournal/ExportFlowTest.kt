package com.personaljournal

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Placeholder instrumentation test for export/share flows (Export Center + Analytics).
 * Marked @Ignore until wired to a device/emulator.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ExportFlowTest {

    @Ignore("Requires device/emulator and UI script")
    @Test
    fun exportShare_opensSafPicker() {
        // TODO: Use Compose UI test to:
        // - open ExportCenterScreen
        // - tap PDF/CSV/JSON export and ensure SAF intent is fired (verify intent count)
        // - ensure buttons disable while exportInProgress state is true
    }
}
