package com.personaljournal.presentation.viewmodel

import com.personaljournal.domain.model.NotificationEvent
import com.personaljournal.domain.model.NotificationStatus
import com.personaljournal.domain.model.ReminderSchedule
import com.personaljournal.domain.model.ReminderType
import com.personaljournal.domain.repository.NotificationHistoryRepository
import com.personaljournal.domain.repository.ReminderRepository
import com.personaljournal.domain.usecase.reminder.ManageReminderUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Rule
import com.personaljournal.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `toggleReminder schedules daily and logs event`() = runTest {
        val reminderRepo = FakeReminderRepo()
        val manage = ManageReminderUseCase(reminderRepo)
        val repo = FakeHistoryRepo()
        val viewModel = NotificationsViewModel(manage, repo)

        viewModel.toggleReminder(id = "daily", enabled = true)
        advanceUntilIdle()

        assertEquals(true, reminderRepo.last?.enabled)
        val state = viewModel.state.first()
        val latest = repo.logged.lastOrNull()
        assertEquals(NotificationStatus.SCHEDULED, latest?.status)
        assertEquals(true, state.toggles.first { it.id == "daily" }.enabled)
    }

    @Test
    fun `clearHistory delegates to repo`() = runTest {
        val repo = FakeHistoryRepo()
        val viewModel = NotificationsViewModel(ManageReminderUseCase(FakeReminderRepo()), repo)

        viewModel.clearHistory()
        advanceUntilIdle()

        assertEquals(true, repo.clearCalled)
    }

    private class FakeReminderRepo : ReminderRepository {
        val flow = MutableSharedFlow<ReminderSchedule?>(replay = 1)
        var last: ReminderSchedule? = null
        var cancelled = false
        override fun reminder() = flow
        override suspend fun schedule(reminder: ReminderSchedule) {
            last = reminder
            flow.emit(reminder)
        }
        override suspend fun cancel() {
            cancelled = true
            flow.emit(null)
        }
    }

    private class FakeHistoryRepo : NotificationHistoryRepository {
        val historyFlow = MutableStateFlow<List<NotificationEvent>>(emptyList())
        val logged = mutableListOf<NotificationEvent>()
        var clearCalled = false
        override fun observeHistory() = historyFlow
        override suspend fun logEvent(title: String, message: String, status: NotificationStatus) {
            val event = NotificationEvent(
                title = title,
                message = message,
                status = status,
                timestamp = kotlinx.datetime.Instant.DISTANT_PAST
            )
            logged.add(event)
            historyFlow.value = listOf(event)
        }
        override suspend fun clear() {
            clearCalled = true
            historyFlow.value = emptyList()
        }
    }
}
