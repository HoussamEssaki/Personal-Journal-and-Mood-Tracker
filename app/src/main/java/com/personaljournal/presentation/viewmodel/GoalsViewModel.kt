package com.personaljournal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personaljournal.domain.model.AchievementBadge
import com.personaljournal.domain.model.GoalProgress
import com.personaljournal.domain.model.HabitTracker
import com.personaljournal.domain.usecase.goals.AddGoalUseCase
import com.personaljournal.domain.usecase.goals.AddHabitUseCase
import com.personaljournal.domain.usecase.goals.IncrementGoalUseCase
import com.personaljournal.domain.usecase.goals.ObserveAchievementsUseCase
import com.personaljournal.domain.usecase.goals.ObserveGoalsUseCase
import com.personaljournal.domain.usecase.goals.ObserveHabitsUseCase
import com.personaljournal.domain.usecase.goals.ToggleHabitCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GoalsUiState(
    val goals: List<GoalProgress> = emptyList(),
    val habits: List<HabitTracker> = emptyList(),
    val achievements: List<AchievementBadge> = emptyList()
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    observeGoalsUseCase: ObserveGoalsUseCase,
    observeHabitsUseCase: ObserveHabitsUseCase,
    observeAchievementsUseCase: ObserveAchievementsUseCase,
    private val incrementGoalUseCase: IncrementGoalUseCase,
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
    private val addGoalUseCase: AddGoalUseCase,
    private val addHabitUseCase: AddHabitUseCase
) : ViewModel() {

    private val refreshing = MutableStateFlow(false)

    val state: StateFlow<GoalsUiState> = combine(
        observeGoalsUseCase(),
        observeHabitsUseCase(),
        observeAchievementsUseCase()
    ) { goals, habits, achievements ->
        GoalsUiState(goals = goals, habits = habits, achievements = achievements)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, GoalsUiState())

    fun incrementGoal(goalId: String) {
        viewModelScope.launch { incrementGoalUseCase(goalId) }
    }

    fun toggleHabit(habitId: String, dayIndex: Int) {
        viewModelScope.launch { toggleHabitCompletionUseCase(habitId, dayIndex) }
    }

    fun addGoal(title: String, target: Int, unit: String) {
        viewModelScope.launch { addGoalUseCase(title, target, unit) }
    }

    fun addHabit(label: String) {
        viewModelScope.launch { addHabitUseCase(label) }
    }
}
