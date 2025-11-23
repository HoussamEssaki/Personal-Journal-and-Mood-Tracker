package com.personaljournal.data.local.mappers

import com.personaljournal.data.local.room.entity.EntryTagCrossRef
import com.personaljournal.data.local.room.entity.JournalEntryEntity
import com.personaljournal.data.local.room.entity.JournalEntryWithRelations
import com.personaljournal.data.local.room.entity.MediaAttachmentEntity
import com.personaljournal.data.local.room.entity.TagEntity
import com.personaljournal.domain.model.AttachmentType
import com.personaljournal.domain.model.FactorCategory
import com.personaljournal.domain.model.GeoTag
import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.model.MediaAttachment
import com.personaljournal.domain.model.Mood
import com.personaljournal.domain.model.MoodFactor
import com.personaljournal.domain.model.MoodLevel
import com.personaljournal.domain.model.Prompt
import com.personaljournal.domain.model.RichTextBlock
import com.personaljournal.domain.model.RichTextContent
import com.personaljournal.domain.model.Tag
import com.personaljournal.domain.model.WeatherSnapshot
import com.personaljournal.util.toEpochMillisCompat
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

@Serializable
private data class RichTextPayload(
    val text: String,
    val bold: Boolean,
    val italic: Boolean,
    val bullet: Boolean
)

fun JournalEntryWithRelations.toDomain(): JournalEntry {
    val prompt = entry.promptId?.let {
        Prompt(
            id = it,
            title = entry.promptTitle.orEmpty(),
            description = entry.promptDescription.orEmpty()
        )
    }
    val moodEntity = requireNotNull(mood) { "Entry must have a mood" }
    val moodDomain = Mood(
        id = moodEntity.id,
        label = moodEntity.label,
        level = MoodLevel.valueOf(moodEntity.level),
        emoji = moodEntity.emoji,
        colorHex = moodEntity.colorHex
    )
    val media = attachments.map { it.toDomainAttachment() }
    val tagsDomain = tags.map { Tag(it.id, it.label, it.colorHex) }
    val location = entry.locationLat?.let {
        GeoTag(
            latitude = it,
            longitude = entry.locationLon ?: 0.0,
            placeName = entry.locationName
        )
    }
    val weather = entry.weatherTemp?.let {
        WeatherSnapshot(it, entry.weatherCondition.orEmpty())
    }
    return JournalEntry(
        id = entry.id,
        title = entry.title,
        content = entry.content,
        richText = entry.richTextJson.toRichText(),
        createdAt = Instant.fromEpochMilliseconds(entry.createdAtEpoch),
        updatedAt = Instant.fromEpochMilliseconds(entry.updatedAtEpoch),
        mood = moodDomain,
        prompt = prompt,
        tags = tagsDomain,
        media = media,
        factors = entry.factors.map { MoodFactor(it, it, FactorCategory.HABIT) },
        secondaryEmotions = entry.secondaryEmotions,
        location = location,
        weather = weather,
        isEncrypted = entry.isEncrypted,
        isSynced = entry.isSynced,
        isPinned = entry.isPinned
    )
}

fun JournalEntry.toEntity(): JournalEntryEntity = JournalEntryEntity(
    id = id,
    title = title,
    content = content,
    richTextJson = richText.toJson(),
    createdAtEpoch = createdAt.toEpochMillisCompat(),
    updatedAtEpoch = updatedAt.toEpochMillisCompat(),
    moodId = mood.id,
    moodLevel = mood.level.name,
    promptId = prompt?.id,
    promptTitle = prompt?.title,
    promptDescription = prompt?.description,
    mediaPaths = media.map { it.filePath },
    tags = tags.map { it.label },
    tagsSearchable = tags.joinToString(",") { it.label.lowercase() },
    secondaryEmotions = secondaryEmotions,
    factors = factors.map { it.label },
    isEncrypted = isEncrypted,
    isSynced = isSynced,
    locationLat = location?.latitude,
    locationLon = location?.longitude,
    locationName = location?.placeName,
    weatherTemp = weather?.temperatureCelsius,
    weatherCondition = weather?.condition,
    isPinned = isPinned
)

fun JournalEntry.mediaEntities(entryId: Long): List<MediaAttachmentEntity> =
    media.map {
        MediaAttachmentEntity(
            attachmentId = it.id,
            entryId = entryId,
            type = it.type.name,
            filePath = it.filePath,
            thumbnailPath = it.thumbnailPath,
            durationSeconds = it.durationSeconds,
            createdAtEpoch = it.createdAt.toEpochMillisCompat()
        )
    }

fun JournalEntry.tagsToCrossRefs(entryId: Long, resolvedTags: List<TagEntity>): List<EntryTagCrossRef> {
    val labelToId = resolvedTags.associateBy({ it.label.lowercase() }, TagEntity::id)
    return tags.mapNotNull { tag ->
        val id = labelToId[tag.label.lowercase()] ?: tag.id.takeIf { it > 0 }
        id?.let { EntryTagCrossRef(entryId = entryId, tagId = it) }
    }
}

fun MediaAttachmentEntity.toDomainAttachment(): MediaAttachment = MediaAttachment(
    id = attachmentId,
    type = AttachmentType.valueOf(type),
    filePath = filePath,
    thumbnailPath = thumbnailPath,
    durationSeconds = durationSeconds,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpoch)
)

private fun RichTextContent.toJson(): String =
    json.encodeToString(
        blocks.map {
            RichTextPayload(
                text = it.text,
                bold = it.bold,
                italic = it.italic,
                bullet = it.bullet
            )
        }
    )

private fun String.toRichText(): RichTextContent =
    runCatching {
        val payload = json.decodeFromString<List<RichTextPayload>>(this)
        RichTextContent(
            blocks = payload.map {
                RichTextBlock(
                    text = it.text,
                    bold = it.bold,
                    italic = it.italic,
                    bullet = it.bullet
                )
            }
        )
    }.getOrDefault(RichTextContent())
