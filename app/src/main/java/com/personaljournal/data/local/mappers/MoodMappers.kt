package com.personaljournal.data.local.mappers

import com.personaljournal.data.local.room.entity.MoodEntity
import com.personaljournal.domain.model.Mood
import com.personaljournal.domain.model.MoodLevel

fun MoodEntity.toDomain(): Mood = Mood(
    id = id,
    label = label,
    level = MoodLevel.valueOf(level),
    emoji = emoji,
    colorHex = colorHex
)

fun Mood.toEntity(): MoodEntity = MoodEntity(
    id = id,
    label = label,
    level = level.name,
    emoji = emoji,
    colorHex = colorHex
)
