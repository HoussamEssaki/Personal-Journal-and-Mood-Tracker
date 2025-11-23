package com.personaljournal.domain.usecase.goals

import com.personaljournal.domain.repository.GoalsRepository
import javax.inject.Inject

class AddGoalUseCase @Inject constructor(
    private val repository: GoalsRepository
    ) {
    suspend operator fun invoke(title: String, target: Int, unitLabel: String) {
        repository.addGoal(title, target, unitLabel)
    }
}
