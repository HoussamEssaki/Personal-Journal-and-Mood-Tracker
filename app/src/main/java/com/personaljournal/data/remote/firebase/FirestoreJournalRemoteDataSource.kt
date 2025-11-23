package com.personaljournal.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.personaljournal.domain.model.AttachmentType
import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.model.MediaAttachment
import com.personaljournal.domain.model.Mood
import com.personaljournal.domain.model.MoodLevel
import com.personaljournal.domain.model.Prompt
import com.personaljournal.domain.model.RichTextContent
import com.personaljournal.domain.model.Tag
import com.personaljournal.util.AppLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Singleton
class FirestoreJournalRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore?,
    private val logger: AppLogger
) : JournalRemoteDataSource {

    private val collection get() = firestore?.collection("journalEntries")

    override suspend fun pushPending(entries: List<JournalEntry>) {
        val collection = collection ?: run {
            logger.d("remote_push", "Skipping remote sync. Firebase is not configured.")
            return
        }
        entries.forEach { entry ->
            runCatching {
                collection.document(entry.id.toString())
                    .set(entry.toPayload())
                    .await()
            }.onFailure { logger.e("remote_push", it) }
        }
    }

    override suspend fun pullUpdates(): List<JournalEntry> = runCatching {
        val collection = collection ?: run {
            logger.d("remote_pull", "Skipping remote sync. Firebase is not configured.")
            return emptyList()
        }
        collection.get().await().documents.mapNotNull { snapshot ->
            snapshot.toObject<RemoteEntryPayload>()?.toDomain(snapshot.id.toLong())
        }
    }.onFailure { logger.e("remote_pull", it) }
        .getOrDefault(emptyList())

    private fun JournalEntry.toPayload(): RemoteEntryPayload = RemoteEntryPayload(
        title = title,
        content = content,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        moodId = mood.id,
        moodLabel = mood.label,
        moodLevel = mood.level.name,
        moodEmoji = mood.emoji,
        moodColor = mood.colorHex,
        tags = tags.map { it.label },
        promptTitle = prompt?.title,
        promptDescription = prompt?.description,
        media = media.map {
            RemoteMediaPayload(
                id = it.id,
                type = it.type.name,
                path = it.filePath,
                thumb = it.thumbnailPath,
                duration = it.durationSeconds
            )
        }
    )

    private fun RemoteEntryPayload.toDomain(id: Long): JournalEntry {
        val mood = Mood(
            id = moodId,
            label = moodLabel,
            level = MoodLevel.valueOf(moodLevel),
            emoji = moodEmoji,
            colorHex = moodColor
        )
        return JournalEntry(
            id = id,
            title = title,
            content = content,
            richText = RichTextContent(),
            createdAt = Instant.parse(createdAt),
            updatedAt = Instant.parse(updatedAt),
            mood = mood,
            tags = tags.map { Tag(label = it) },
            prompt = promptTitle?.let {
                Prompt(id = "$id", title = it, description = promptDescription.orEmpty())
            },
            media = media.map {
                MediaAttachment(
                    id = it.id,
                    type = AttachmentType.valueOf(it.type),
                    filePath = it.path,
                    thumbnailPath = it.thumb,
                    durationSeconds = it.duration,
                    createdAt = Clock.System.now()
                )
            }
        )
    }

    data class RemoteEntryPayload(
        val title: String = "",
        val content: String = "",
        val createdAt: String = Clock.System.now().toString(),
        val updatedAt: String = Clock.System.now().toString(),
        val moodId: Long = 0,
        val moodLabel: String = "",
        val moodLevel: String = MoodLevel.NEUTRAL.name,
        val moodEmoji: String = "",
        val moodColor: String = "#4A90E2",
        val tags: List<String> = emptyList(),
        val promptTitle: String? = null,
        val promptDescription: String? = null,
        val media: List<RemoteMediaPayload> = emptyList()
    )

    data class RemoteMediaPayload(
        val id: String = "",
        val type: String = AttachmentType.PHOTO.name,
        val path: String = "",
        val thumb: String? = null,
        val duration: Int? = null
    )
}
