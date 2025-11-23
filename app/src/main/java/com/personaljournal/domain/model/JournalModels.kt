package com.personaljournal.domain.model

import kotlinx.datetime.Instant

data class JournalEntry(
    val id: Long = 0L,
    val title: String,
    val content: String,
    val richText: RichTextContent = RichTextContent(),
    val createdAt: Instant,
    val updatedAt: Instant = createdAt,
    val mood: Mood,
    val prompt: Prompt? = null,
    val tags: List<Tag> = emptyList(),
    val media: List<MediaAttachment> = emptyList(),
    val factors: List<MoodFactor> = emptyList(),
    val secondaryEmotions: List<String> = emptyList(),
    val location: GeoTag? = null,
    val weather: WeatherSnapshot? = null,
    val isEncrypted: Boolean = true,
    val isSynced: Boolean = false,
    val isPinned: Boolean = false
)

data class MediaAttachment(
    val id: String,
    val type: AttachmentType,
    val filePath: String,
    val thumbnailPath: String? = null,
    val durationSeconds: Int? = null,
    val createdAt: Instant = Instant.DISTANT_PAST
)

enum class AttachmentType { PHOTO, AUDIO }

data class Prompt(
    val id: String,
    val title: String,
    val description: String,
    val locale: String = "en"
)

data class GeoTag(
    val latitude: Double,
    val longitude: Double,
    val placeName: String? = null
)

data class WeatherSnapshot(
    val temperatureCelsius: Double,
    val condition: String
)

data class RichTextContent(
    val blocks: List<RichTextBlock> = emptyList()
)

data class RichTextBlock(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val bullet: Boolean = false
)
