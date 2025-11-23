package com.personaljournal.data.repository

import com.personaljournal.data.local.mappers.toDomain
import com.personaljournal.data.local.mappers.toEntity
import com.personaljournal.data.local.room.dao.TagDao
import com.personaljournal.domain.model.Tag
import com.personaljournal.domain.repository.TagRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
) : TagRepository {

    override fun observeTags(): Flow<List<Tag>> = tagDao.observe().map { tags ->
        tags.map { it.toDomain() }
    }

    override suspend fun getAll(): List<Tag> = tagDao.snapshot().map { it.toDomain() }

    override suspend fun upsert(tag: Tag): Long = tagDao.upsert(tag.toEntity())

    override suspend fun delete(tagId: Long) = tagDao.deleteById(tagId)

    override suspend fun seedDefaults() {
        val defaults = listOf(
            Tag(label = "#gratitude"),
            Tag(label = "#work"),
            Tag(label = "#family"),
            Tag(label = "#health")
        )
        defaults.forEach { upsert(it) }
    }
}
