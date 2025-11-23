package com.personaljournal.domain.usecase.goals

import com.personaljournal.domain.model.GoalProgress
import com.personaljournal.domain.repository.GoalsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveGoalsUseCase @Inject constructor(
    private val repository: GoalsRepository
) {
    operator fun invoke(): Flow<List<GoalProgress>> = repository.observeGoals()
}
