package com.personaljournal.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "entry_tag_cross_ref",
    primaryKeys = ["entryId", "tagId"],
    indices = [
        Index(value = ["tagId"]),
        Index(value = ["entryId"])
    ]
)
data class EntryTagCrossRef(
    val entryId: Long,
    val tagId: Long
)
