package com.personaljournal.domain.model

data class Tag(
    val id: Long = 0L,
    val label: String,
    val colorHex: String = "#4A90E2"
)

data class EntryFilter(
    val query: String? = null,
    val startDateIso: String? = null,
    val endDateIso: String? = null,
    val moodLevels: Set<MoodLevel> = emptySet(),
    val tagLabels: Set<String> = emptySet(),
    val hasMedia: Boolean? = null
)
