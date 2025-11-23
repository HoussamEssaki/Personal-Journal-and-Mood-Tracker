package com.personaljournal.domain.usecase.goals

import com.personaljournal.domain.model.AchievementBadge
import com.personaljournal.domain.repository.GoalsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveAchievementsUseCase @Inject constructor(
    private val repository: GoalsRepository
) {
    operator fun invoke(): Flow<List<AchievementBadge>> = repository.observeAchievements()
}
