package com.personaljournal

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Placeholder instrumentation test for notification history pagination and filters.
 * Marked @Ignore until device/emulator automation is added.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class NotificationHistoryTest {

    @Ignore("Requires device/emulator and UI script")
    @Test
    fun historyPagination_andFilters() {
        // TODO: Compose UI test:
        // - seed history items (WorkManager log repo)
        // - expand history, paginate (Show more), apply filter chips, clear history confirmation
    }
}
