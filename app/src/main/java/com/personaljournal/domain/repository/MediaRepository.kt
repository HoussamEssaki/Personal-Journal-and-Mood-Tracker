package com.personaljournal.domain.repository

import com.personaljournal.domain.model.AttachmentType
import com.personaljournal.domain.model.MediaAttachment
import kotlinx.coroutines.flow.Flow
import java.io.File

interface MediaRepository {
    suspend fun save(bytes: ByteArray, type: AttachmentType): MediaAttachment
    suspend fun delete(attachmentId: String)
    fun observeAttachments(entryId: Long): Flow<List<MediaAttachment>>
    suspend fun getFile(attachment: MediaAttachment): File
}
