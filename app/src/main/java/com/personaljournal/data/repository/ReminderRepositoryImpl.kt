package com.personaljournal.data.repository

import com.personaljournal.domain.model.ReminderSchedule
import com.personaljournal.domain.model.ReminderType
import com.personaljournal.domain.repository.ReminderRepository
import com.personaljournal.infrastructure.reminders.ReminderScheduler
import com.personaljournal.infrastructure.security.SecurePreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val scheduler: ReminderScheduler,
    private val securePreferences: SecurePreferences
) : ReminderRepository {

    private val reminderFlow = MutableStateFlow(load())

    override fun reminder(): Flow<ReminderSchedule?> = reminderFlow.asStateFlow()

    override suspend fun schedule(schedule: ReminderSchedule) {
        val clamped = schedule.copy(
            hourOfDay = schedule.hourOfDay.coerceIn(0, 23),
            minute = schedule.minute.coerceIn(0, 59)
        )
        save(clamped)
        scheduler.schedule(clamped)
        reminderFlow.value = clamped
    }

    override suspend fun cancel() {
        scheduler.cancel()
        securePreferences.putBoolean(KEY_REMINDER_ENABLED, false)
        reminderFlow.value = null
    }

    private fun load(): ReminderSchedule? {
        val enabled = securePreferences.getBoolean(KEY_REMINDER_ENABLED)
        if (!enabled) return null
        val type = ReminderType.valueOf(
            securePreferences.getString(KEY_REMINDER_TYPE, ReminderType.DAILY.name)
        )
        return ReminderSchedule(
            enabled = true,
            type = type,
            hourOfDay = securePreferences.getLong(KEY_REMINDER_HOUR, 20).toInt(),
            minute = securePreferences.getLong(KEY_REMINDER_MINUTE, 0).toInt(),
            dayOfWeek = securePreferences.getLong(KEY_REMINDER_DAY, 1).toInt(),
            motivationalMessage = securePreferences.getString(KEY_REMINDER_MESSAGE).ifEmpty { null }
        )
    }

    private fun save(schedule: ReminderSchedule) {
        securePreferences.putBoolean(KEY_REMINDER_ENABLED, schedule.enabled)
        securePreferences.putString(KEY_REMINDER_TYPE, schedule.type.name)
        securePreferences.putLong(KEY_REMINDER_HOUR, schedule.hourOfDay.toLong())
        securePreferences.putLong(KEY_REMINDER_MINUTE, schedule.minute.toLong())
        securePreferences.putLong(KEY_REMINDER_DAY, (schedule.dayOfWeek ?: 1).toLong())
        schedule.motivationalMessage?.let { securePreferences.putString(KEY_REMINDER_MESSAGE, it) }
    }

    companion object {
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_TYPE = "reminder_type"
        private const val KEY_REMINDER_HOUR = "reminder_hour"
        private const val KEY_REMINDER_MINUTE = "reminder_minute"
        private const val KEY_REMINDER_DAY = "reminder_day"
        private const val KEY_REMINDER_MESSAGE = "reminder_message"
    }
}
