package com.personaljournal.domain.usecase.tag

import com.personaljournal.domain.model.Tag
import com.personaljournal.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpsertTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(tag: Tag): Long = tagRepository.upsert(tag)
    fun observe(): Flow<List<Tag>> = tagRepository.observeTags()
    suspend fun delete(id: Long) = tagRepository.delete(id)
}
