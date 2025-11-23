package com.personaljournal.data.local.room.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class JournalEntryWithRelations(
    @Embedded val entry: JournalEntryEntity,
    @Relation(
        parentColumn = "moodId",
        entityColumn = "id"
    )
    val mood: MoodEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EntryTagCrossRef::class,
            parentColumn = "entryId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "entryId"
    )
    val attachments: List<MediaAttachmentEntity>
)
