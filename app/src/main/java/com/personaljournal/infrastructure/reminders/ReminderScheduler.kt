package com.personaljournal.infrastructure.reminders

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.personaljournal.domain.model.ReminderSchedule
import com.personaljournal.domain.model.ReminderType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager by lazy { WorkManager.getInstance(context) }

    fun schedule(schedule: ReminderSchedule) {
        val repeatInterval = when (schedule.type) {
            ReminderType.DAILY -> Duration.ofDays(1)
            ReminderType.WEEKLY -> Duration.ofDays(7)
        }
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(
            repeatInterval.toHours(), TimeUnit.HOURS
        )
            .addTag(WORK_TAG)
            .setInputData(
                workDataOf(
                    "motivation" to (schedule.motivationalMessage ?: DEFAULT_MESSAGE)
                )
            )
            .build()
        workManager.enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel() {
        workManager.cancelAllWorkByTag(WORK_TAG)
    }

    companion object {
        const val WORK_TAG = "journal_reminder"
        private const val DEFAULT_MESSAGE = "Take a mindful minute and jot down how you feel."
    }
}
