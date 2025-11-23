package com.personaljournal.domain.usecase.media

import com.personaljournal.domain.model.MediaAttachment
import com.personaljournal.domain.repository.MediaRepository
import java.io.File
import javax.inject.Inject

class GetMediaFileUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(attachment: MediaAttachment): File =
        mediaRepository.getFile(attachment)
}
