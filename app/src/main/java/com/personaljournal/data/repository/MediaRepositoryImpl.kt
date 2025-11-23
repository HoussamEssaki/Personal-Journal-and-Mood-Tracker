package com.personaljournal.data.repository

import com.personaljournal.data.local.mappers.toDomainAttachment
import com.personaljournal.data.local.room.dao.MediaAttachmentDao
import com.personaljournal.domain.model.AttachmentType
import com.personaljournal.domain.model.MediaAttachment
import com.personaljournal.domain.repository.MediaRepository
import com.personaljournal.infrastructure.storage.EncryptedMediaStorage
import com.personaljournal.util.AppLogger
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val storage: EncryptedMediaStorage,
    private val mediaAttachmentDao: MediaAttachmentDao,
    private val logger: AppLogger,
    @com.personaljournal.di.IoDispatcher private val dispatcher: kotlinx.coroutines.CoroutineDispatcher
) : MediaRepository {

    override suspend fun save(bytes: ByteArray, type: AttachmentType): MediaAttachment =
        withContext(dispatcher) {
            val extension = when (type) {
                AttachmentType.PHOTO -> "jpg"
                AttachmentType.AUDIO -> "aac"
            }
            val file = storage.save(bytes, extension)
            MediaAttachment(
                id = UUID.randomUUID().toString(),
                type = type,
                filePath = file.absolutePath,
                createdAt = Clock.System.now()
            )
        }

    override suspend fun delete(attachmentId: String) = withContext(dispatcher) {
        val entity = mediaAttachmentDao.getById(attachmentId) ?: return@withContext
        runCatching { storage.delete(File(entity.filePath)) }
            .onFailure { logger.e("media_delete", it) }
        mediaAttachmentDao.delete(attachmentId)
    }

    override fun observeAttachments(entryId: Long): Flow<List<MediaAttachment>> =
        mediaAttachmentDao.observe(entryId).map { items ->
            items.map { it.toDomainAttachment() }
        }

    override suspend fun getFile(attachment: MediaAttachment): File =
        storage.decryptToTemp(storage.fileFromPath(attachment.filePath))
}
