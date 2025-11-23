package com.personaljournal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.model.Mood
import com.personaljournal.domain.model.Prompt
import com.personaljournal.domain.model.StatsSnapshot
import com.personaljournal.domain.model.LayoutPreferences
import com.personaljournal.domain.model.LayoutPreset
import com.personaljournal.domain.model.PersonalPreferences
import com.personaljournal.domain.usecase.analytics.GenerateStatsUseCase
import com.personaljournal.domain.usecase.journal.ObserveJournalEntriesUseCase
import com.personaljournal.domain.usecase.mood.ObserveMoodsUseCase
import com.personaljournal.domain.usecase.prompts.GetDailyPromptUseCase
import com.personaljournal.domain.usecase.goals.ObserveGoalsUseCase
import com.personaljournal.domain.usecase.goals.ObserveHabitsUseCase
import com.personaljournal.domain.usecase.settings.PersonalizationSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val entries: List<JournalEntry> = emptyList(),
    val moods: List<Mood> = emptyList(),
    val stats: StatsSnapshot = StatsSnapshot(),
    val prompt: Prompt? = null,
    val selectedMood: Mood? = null,
    val goals: List<com.personaljournal.domain.model.GoalProgress> = emptyList(),
    val habits: List<com.personaljournal.domain.model.HabitTracker> = emptyList(),
    val layout: LayoutPreferences = LayoutPreferences(),
    val personal: PersonalPreferences = PersonalPreferences()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeJournalEntriesUseCase: ObserveJournalEntriesUseCase,
    observeMoodsUseCase: ObserveMoodsUseCase,
    generateStatsUseCase: GenerateStatsUseCase,
    getDailyPromptUseCase: GetDailyPromptUseCase,
    personalizationSettingsUseCase: PersonalizationSettingsUseCase,
    observeGoalsUseCase: ObserveGoalsUseCase,
    observeHabitsUseCase: ObserveHabitsUseCase
) : ViewModel() {

    private val promptFlow = MutableStateFlow<Prompt?>(null)
    private val selectedMoodFlow = MutableStateFlow<Mood?>(null)
    private val layoutFlow = personalizationSettingsUseCase.layout()
    private val personalFlow = personalizationSettingsUseCase.personal()
    private val goalsFlow = observeGoalsUseCase()
    private val habitsFlow = observeHabitsUseCase()

    private val baseState = combine(
        observeJournalEntriesUseCase(),
        observeMoodsUseCase(),
        generateStatsUseCase.observe(),
        promptFlow,
        selectedMoodFlow
    ) { entries, moods, stats, prompt, selectedMood ->
        DashboardUiState(
            entries = entries,
            moods = moods,
            stats = stats,
            prompt = prompt,
            selectedMood = selectedMood ?: moods.firstOrNull()
        )
    }.combine(goalsFlow) { base, goals ->
        base.copy(goals = goals)
    }.combine(habitsFlow) { base, habits ->
        base.copy(habits = habits)
    }

    val state: StateFlow<DashboardUiState> = combine(
        baseState,
        layoutFlow,
        personalFlow
    ) { base, layout, personal ->
        base.copy(layout = layout, personal = personal)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DashboardUiState())

    init {
        viewModelScope.launch {
            getDailyPromptUseCase("en").collect { promptFlow.value = it }
        }
    }

    fun selectMood(mood: Mood) {
        selectedMoodFlow.value = mood
    }
}
