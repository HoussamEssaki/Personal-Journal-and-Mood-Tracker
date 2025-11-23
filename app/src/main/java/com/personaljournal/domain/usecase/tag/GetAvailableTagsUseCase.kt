package com.personaljournal.domain.usecase.tag

import com.personaljournal.domain.model.Tag
import com.personaljournal.domain.repository.TagRepository
import javax.inject.Inject

class GetAvailableTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(): List<Tag> = tagRepository.getAll()
}
