package com.personaljournal.infrastructure.reminders

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.personaljournal.domain.model.NotificationStatus
import com.personaljournal.domain.repository.NotificationHistoryRepository
import com.personaljournal.util.Notifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notifier: Notifier,
    private val historyRepository: NotificationHistoryRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val message = inputData.getString("motivation")
            ?: "Time to check in with yourself."
        notifier.showReminder(message)
        val localTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val timestamp = "${localTime.hour.toString().padStart(2, '0')}:${localTime.minute.toString().padStart(2, '0')}"
        historyRepository.logEvent(
            title = "Daily journaling reminder",
            message = "$message (sent at $timestamp)",
            status = NotificationStatus.DELIVERED
        )
        return Result.success()
    }
}
