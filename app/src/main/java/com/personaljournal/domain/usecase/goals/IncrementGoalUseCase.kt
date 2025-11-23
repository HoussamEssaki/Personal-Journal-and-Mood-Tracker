package com.personaljournal.domain.usecase.goals

import com.personaljournal.domain.repository.GoalsRepository
import javax.inject.Inject

class IncrementGoalUseCase @Inject constructor(
    private val repository: GoalsRepository
) {
    suspend operator fun invoke(goalId: String) = repository.incrementGoal(goalId)
}
