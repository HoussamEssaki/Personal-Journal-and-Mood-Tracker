package com.personaljournal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaljournal.domain.model.NotificationEvent
import com.personaljournal.domain.model.NotificationStatus
import com.personaljournal.domain.model.ReminderSchedule
import com.personaljournal.domain.model.ReminderType
import com.personaljournal.domain.repository.NotificationHistoryRepository
import com.personaljournal.domain.usecase.reminder.ManageReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class ReminderToggleUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val enabled: Boolean,
    val motivationalMessage: String? = null
)

data class NotificationsUiState(
    val toggles: List<ReminderToggleUi> = emptyList(),
    val history: List<NotificationEvent> = emptyList()
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val manageReminderUseCase: ManageReminderUseCase,
    private val historyRepository: NotificationHistoryRepository
) : ViewModel() {

    private val manualToggles = MutableStateFlow(
        listOf(
            ReminderToggleUi("daily", "Daily Journaling", "10:00 AM", true),
            ReminderToggleUi("mood", "Mood Check-in", "3 times a day", true),
            ReminderToggleUi("goals", "Goal Milestones", "Weekly progress alerts", false)
        )
    )

    val state: StateFlow<NotificationsUiState> = combine(
        manualToggles,
        historyRepository.observeHistory()
    ) { toggles, history ->
        NotificationsUiState(
            toggles = toggles,
            history = history.sortedByDescending { it.timestamp }
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, NotificationsUiState())

    init {
        viewModelScope.launch {
            manageReminderUseCase.observe().collect { schedule ->
                updateDailyToggle(schedule)
            }
        }
    }

    fun toggleReminder(id: String, enabled: Boolean) {
        if (id == "daily") {
            viewModelScope.launch {
                if (enabled) {
                    manageReminderUseCase.schedule(
                        ReminderSchedule(
                            enabled = true,
                            type = ReminderType.DAILY,
                            hourOfDay = 10,
                            minute = 0,
                            motivationalMessage = DEFAULT_MOTIVATION
                        )
                    )
                } else {
                    manageReminderUseCase.cancel()
                }
            }
        }
        manualToggles.value = manualToggles.value.map { toggle ->
            if (toggle.id == id) toggle.copy(enabled = enabled) else toggle
        }
        logEvent(
            title = manualToggles.value.firstOrNull { it.id == id }?.title
                ?: "Reminder",
            status = if (enabled) NotificationStatus.SCHEDULED else NotificationStatus.PAUSED,
            message = if (enabled) "Reminder scheduled" else "Reminder paused"
        )
    }

    fun clearHistory() {
        viewModelScope.launch { historyRepository.clear() }
    }

    private fun updateDailyToggle(schedule: ReminderSchedule?) {
        manualToggles.value = manualToggles.value.map { toggle ->
            if (toggle.id == "daily") {
                toggle.copy(
                    enabled = schedule?.enabled == true,
                    subtitle = schedule?.let { "${pad(it.hourOfDay)}:${pad(it.minute)}" } ?: "Not scheduled",
                    motivationalMessage = schedule?.motivationalMessage ?: DEFAULT_MOTIVATION
                )
            } else toggle
        }
        schedule?.let {
            logEvent(
                title = "Daily Journaling",
                status = if (it.enabled) NotificationStatus.SCHEDULED else NotificationStatus.CANCELLED,
                message = if (it.enabled) "Scheduled at ${pad(it.hourOfDay)}:${pad(it.minute)}" else "Not scheduled"
            )
        }
    }

    private fun logEvent(title: String, status: NotificationStatus, message: String) {
        viewModelScope.launch {
            val timestamp = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .let { "${it.month.name.lowercase().replaceFirstChar { c -> c.titlecase() }} ${it.dayOfMonth}" }
            historyRepository.logEvent(
                title = title,
                message = "$message • $timestamp",
                status = status
            )
        }
    }

    private fun pad(value: Int): String = value.toString().padStart(2, '0')

    companion object {
        private const val DEFAULT_MOTIVATION = "Take a mindful minute and jot down how you feel."
    }
}
