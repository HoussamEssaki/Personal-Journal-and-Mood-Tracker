package com.personaljournal.data.repository

import com.personaljournal.data.local.room.dao.NotificationLogDao
import com.personaljournal.data.local.room.entity.NotificationLogEntity
import com.personaljournal.domain.model.NotificationEvent
import com.personaljournal.domain.model.NotificationStatus
import com.personaljournal.domain.repository.NotificationHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

@Singleton
class NotificationHistoryRepositoryImpl @Inject constructor(
    private val logDao: NotificationLogDao
) : NotificationHistoryRepository {

    override fun observeHistory(): Flow<List<NotificationEvent>> =
        logDao.observeLogs().map { entries ->
            entries.map { it.toDomain() }
        }

    override suspend fun logEvent(title: String, message: String, status: NotificationStatus) {
        withContext(Dispatchers.IO) {
            logDao.insert(
                NotificationLogEntity(
                    title = title,
                    message = message,
                    status = status.name,
                    timestamp = System.currentTimeMillis()
                )
            )
            logDao.trimTo(200)
        }
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            logDao.clearAll()
        }
    }

    private fun NotificationLogEntity.toDomain(): NotificationEvent {
        val parsedStatus = runCatching { NotificationStatus.valueOf(status) }.getOrDefault(NotificationStatus.DELIVERED)
        return NotificationEvent(
            title = title,
            message = message,
            status = parsedStatus,
            timestamp = Instant.fromEpochMilliseconds(timestamp)
        )
    }
}
