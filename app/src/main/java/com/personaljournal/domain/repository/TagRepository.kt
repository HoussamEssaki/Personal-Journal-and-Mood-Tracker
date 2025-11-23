package com.personaljournal.domain.repository

import com.personaljournal.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun observeTags(): Flow<List<Tag>>
    suspend fun getAll(): List<Tag>
    suspend fun upsert(tag: Tag): Long
    suspend fun delete(tagId: Long)
    suspend fun seedDefaults()
}
