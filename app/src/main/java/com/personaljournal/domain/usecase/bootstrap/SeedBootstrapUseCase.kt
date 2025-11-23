package com.personaljournal.domain.usecase.bootstrap

import com.personaljournal.domain.repository.MoodRepository
import com.personaljournal.domain.repository.TagRepository
import javax.inject.Inject

class SeedBootstrapUseCase @Inject constructor(
    private val moodRepository: MoodRepository,
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke() {
        moodRepository.seedDefaults()
        tagRepository.seedDefaults()
    }
}
