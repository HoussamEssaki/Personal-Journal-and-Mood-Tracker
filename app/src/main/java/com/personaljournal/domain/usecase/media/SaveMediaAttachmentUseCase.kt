package com.personaljournal.domain.usecase.media

import com.personaljournal.domain.model.AttachmentType
import com.personaljournal.domain.model.MediaAttachment
import com.personaljournal.domain.repository.MediaRepository
import javax.inject.Inject

class SaveMediaAttachmentUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(bytes: ByteArray, type: AttachmentType): MediaAttachment =
        mediaRepository.save(bytes, type)
}
