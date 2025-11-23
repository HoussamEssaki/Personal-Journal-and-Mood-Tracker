package com.personaljournal.domain.model

import kotlinx.datetime.Instant

enum class NotificationStatus { SCHEDULED, DELIVERED, PAUSED, CANCELLED }

data class NotificationEvent(
    val title: String,
    val message: String,
    val status: NotificationStatus,
    val timestamp: Instant
)
