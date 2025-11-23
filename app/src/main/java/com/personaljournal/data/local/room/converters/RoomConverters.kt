package com.personaljournal.data.local.room.converters

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RoomConverters {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? = list?.let { json.encodeToString(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String> =
        value?.let { json.decodeFromString(it) } ?: emptyList()

    @TypeConverter
    fun fromLongList(list: List<Long>?): String? = list?.let { json.encodeToString(it) }

    @TypeConverter
    fun toLongList(value: String?): List<Long> =
        value?.let { json.decodeFromString(it) } ?: emptyList()

    @TypeConverter
    fun fromBooleanList(list: List<Boolean>?): String? = list?.let { json.encodeToString(it) }

    @TypeConverter
    fun toBooleanList(value: String?): List<Boolean> =
        value?.let { json.decodeFromString(it) } ?: emptyList()
}
