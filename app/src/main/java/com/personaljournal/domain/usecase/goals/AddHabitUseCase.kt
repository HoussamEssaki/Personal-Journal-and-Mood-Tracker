package com.personaljournal.domain.usecase.goals

import com.personaljournal.domain.repository.GoalsRepository
import javax.inject.Inject

class AddHabitUseCase @Inject constructor(
    private val repository: GoalsRepository
) {
    suspend operator fun invoke(label: String) {
        repository.addHabit(label)
    }
}
