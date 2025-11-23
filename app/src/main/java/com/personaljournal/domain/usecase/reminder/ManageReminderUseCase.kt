package com.personaljournal.domain.usecase.reminder

import com.personaljournal.domain.model.ReminderSchedule
import com.personaljournal.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    fun observe(): Flow<ReminderSchedule?> = reminderRepository.reminder()
    suspend fun schedule(schedule: ReminderSchedule) = reminderRepository.schedule(schedule)
    suspend fun cancel() = reminderRepository.cancel()
}
