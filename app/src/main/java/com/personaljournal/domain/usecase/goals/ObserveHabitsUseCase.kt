package com.personaljournal.domain.usecase.goals

import com.personaljournal.domain.model.HabitTracker
import com.personaljournal.domain.repository.GoalsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveHabitsUseCase @Inject constructor(
    private val repository: GoalsRepository
) {
    operator fun invoke(): Flow<List<HabitTracker>> = repository.observeHabits()
}
