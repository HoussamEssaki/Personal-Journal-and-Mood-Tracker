package com.personaljournal.domain.repository

import com.personaljournal.domain.model.NotificationEvent
import com.personaljournal.domain.model.NotificationStatus
import kotlinx.coroutines.flow.Flow

interface NotificationHistoryRepository {
    fun observeHistory(): Flow<List<NotificationEvent>>
    suspend fun logEvent(title: String, message: String, status: NotificationStatus)
    suspend fun clear()
}
