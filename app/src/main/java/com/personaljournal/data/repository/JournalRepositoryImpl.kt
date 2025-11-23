package com.personaljournal.data.repository

import com.personaljournal.data.local.mappers.toDomain
import com.personaljournal.data.local.mappers.toEntity
import com.personaljournal.data.local.mappers.tagsToCrossRefs
import com.personaljournal.data.local.mappers.mediaEntities
import com.personaljournal.data.local.room.dao.JournalEntryDao
import com.personaljournal.data.local.room.dao.MediaAttachmentDao
import com.personaljournal.data.local.room.dao.TagDao
import com.personaljournal.data.remote.firebase.JournalRemoteDataSource
import com.personaljournal.di.IoDispatcher
import com.personaljournal.domain.model.EntryFilter
import com.personaljournal.domain.model.ExportRequest
import com.personaljournal.domain.model.ExportRange
import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.repository.JournalRepository
import com.personaljournal.infrastructure.storage.ReportExporter
import com.personaljournal.util.AppLogger
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.Instant.Companion.fromEpochMilliseconds
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import com.personaljournal.util.toEpochMillisCompat

@Singleton
class JournalRepositoryImpl @Inject constructor(
    private val journalEntryDao: JournalEntryDao,
    private val tagDao: TagDao,
    private val mediaAttachmentDao: MediaAttachmentDao,
    private val remoteDataSource: JournalRemoteDataSource,
    private val reportExporter: ReportExporter,
    private val logger: AppLogger,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : JournalRepository {

    override fun observeEntries(): Flow<List<JournalEntry>> =
        journalEntryDao.observeEntries()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatcher)

    override fun searchEntries(filter: EntryFilter): Flow<List<JournalEntry>> =
        journalEntryDao.search(
            query = filter.query,
            startEpoch = filter.startDateIso?.toEpochMillis(),
            endEpoch = filter.endDateIso?.toEpochMillis(),
            moodLevels = filter.moodLevels.takeIf { it.isNotEmpty() }?.map { it.name },
            hasMedia = filter.hasMedia,
            tagsFilter = filter.tagLabels.takeIf { it.isNotEmpty() }?.joinToString(",")
        ).map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcher)

    override suspend fun getEntry(id: Long): JournalEntry? =
        withContext(dispatcher) {
            journalEntryDao.getById(id)?.toDomain()
        }

    override suspend fun createEntry(entry: JournalEntry): Long = withContext(dispatcher) {
        val entryId = journalEntryDao.insert(entry.copy(id = 0).toEntity())
        persistRelations(entry.copy(id = entryId))
        entryId
    }

    override suspend fun updateEntry(entry: JournalEntry) = withContext(dispatcher) {
        journalEntryDao.update(entry.toEntity())
        persistRelations(entry)
    }

    override suspend fun deleteEntry(id: Long) = withContext(dispatcher) {
        val attachments = mediaAttachmentDao.getByEntry(id)
        attachments.forEach { mediaAttachmentDao.delete(it.attachmentId) }
        journalEntryDao.clearCrossRefs(id)
        journalEntryDao.delete(id)
    }

    override suspend fun upsertAll(entries: List<JournalEntry>) = withContext(dispatcher) {
        entries.forEach { entry ->
            val existing = journalEntryDao.getById(entry.id)
            if (existing == null) {
                createEntry(entry)
            } else {
                updateEntry(entry)
            }
        }
    }

    override suspend fun export(request: ExportRequest): Result<File> =
        withContext(dispatcher) {
            runCatching {
                val entries = journalEntryDao.snapshot()
                    .map { it.toDomain() }
                    .filter { entry ->
                        when (val range = request.range) {
                            ExportRange.Last30Days -> entry.createdAt >= Instant.fromEpochMilliseconds(
                                System.currentTimeMillis() - THIRTY_DAYS_MILLIS
                            )
                            is ExportRange.Custom -> {
                                val startEpoch = range.start.atStartOfDayIn(TimeZone.UTC).toEpochMillisCompat()
                                val endEpoch = range.end.atStartOfDayIn(TimeZone.UTC).toEpochMillisCompat()
                                entry.createdAt.toEpochMillisCompat() in startEpoch..endEpoch
                            }
                        }
                    }
                reportExporter.export(entries, request)
            }.onFailure { logger.e("export", it) }
        }

    override suspend fun syncPendingEntries() = withContext(dispatcher) {
        val pending = journalEntryDao.pendingSync().map { it.toDomain() }
        if (pending.isEmpty()) return@withContext
        remoteDataSource.pushPending(pending)
        remoteDataSource.pullUpdates().also { remoteEntries ->
            upsertAll(remoteEntries.map { it.copy(isSynced = true) })
        }
    }

    override suspend fun setPinned(id: Long, pinned: Boolean) = withContext(dispatcher) {
        journalEntryDao.setPinned(id, pinned)
    }

    private suspend fun persistRelations(entry: JournalEntry) {
        val resolvedTags =
            if (entry.tags.isEmpty()) emptyList()
            else tagDao.findByLabels(entry.tags.map { it.label.lowercase() })
        journalEntryDao.clearCrossRefs(entry.id)
        journalEntryDao.insertCrossRefs(entry.tagsToCrossRefs(entry.id, resolvedTags))
        mediaAttachmentDao.clearEntry(entry.id)
        entry.mediaEntities(entry.id).forEach { mediaAttachmentDao.insert(it) }
    }

    private fun String.toEpochMillis(): Long? =
        runCatching { LocalDate.parse(this).atStartOfDayIn(TimeZone.UTC).toEpochMillisCompat() }
            .getOrNull()

    companion object {
        private const val THIRTY_DAYS_MILLIS = 30L * 24 * 60 * 60 * 1000
    }
}

