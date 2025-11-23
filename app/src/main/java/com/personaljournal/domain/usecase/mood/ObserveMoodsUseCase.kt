package com.personaljournal.domain.usecase.mood

import com.personaljournal.domain.model.Mood
import com.personaljournal.domain.repository.MoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMoodsUseCase @Inject constructor(
    private val moodRepository: MoodRepository
) {
    operator fun invoke(): Flow<List<Mood>> = moodRepository.observeMoods()
}
