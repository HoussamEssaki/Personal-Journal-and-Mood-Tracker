package com.personaljournal.data.local.mappers

import com.personaljournal.data.local.room.entity.TagEntity
import com.personaljournal.domain.model.Tag

fun TagEntity.toDomain(): Tag = Tag(
    id = id,
    label = label,
    colorHex = colorHex
)

fun Tag.toEntity(): TagEntity = TagEntity(
    id = id,
    label = label,
    colorHex = colorHex
)
