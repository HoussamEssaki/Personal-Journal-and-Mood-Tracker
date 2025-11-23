package com.personaljournal.domain.repository

import com.personaljournal.domain.model.ReminderSchedule
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun reminder(): Flow<ReminderSchedule?>
    suspend fun schedule(schedule: ReminderSchedule)
    suspend fun cancel()
}
