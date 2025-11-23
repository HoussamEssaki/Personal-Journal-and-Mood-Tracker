package com.personaljournal.domain.usecase.media

import com.personaljournal.domain.repository.MediaRepository
import javax.inject.Inject

class DeleteMediaAttachmentUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(id: String) = mediaRepository.delete(id)
}
