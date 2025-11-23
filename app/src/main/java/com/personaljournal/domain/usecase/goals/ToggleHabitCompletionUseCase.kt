package com.personaljournal.domain.usecase.goals

import com.personaljournal.domain.repository.GoalsRepository
import javax.inject.Inject

class ToggleHabitCompletionUseCase @Inject constructor(
    private val repository: GoalsRepository
) {
    suspend operator fun invoke(habitId: String, dayIndex: Int) =
        repository.toggleHabitCompletion(habitId, dayIndex)
}
