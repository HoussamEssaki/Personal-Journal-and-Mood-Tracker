package com.personaljournal.domain.usecase.mood

import com.personaljournal.domain.model.Mood
import com.personaljournal.domain.repository.MoodRepository
import javax.inject.Inject

class GetAvailableMoodsUseCase @Inject constructor(
    private val moodRepository: MoodRepository
) {
    suspend operator fun invoke(): List<Mood> = moodRepository.getAll()
}
