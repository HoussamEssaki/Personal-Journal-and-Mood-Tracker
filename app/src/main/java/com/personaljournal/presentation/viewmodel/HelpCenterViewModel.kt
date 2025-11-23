package com.personaljournal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

data class HelpEntry(
    val id: String,
    val title: String,
    val description: String,
    val category: String
)

data class HelpSection(
    val title: String,
    val entries: List<HelpEntry>
)

data class HelpCenterUiState(
    val query: String = "",
    val sections: List<HelpSection> = emptyList(),
    val filteredEntries: List<HelpEntry> = emptyList()
)

@HiltViewModel
class HelpCenterViewModel @Inject constructor() : ViewModel() {

    private val entries = listOf(
        HelpEntry("getting-started", "Getting Started", "Learn how to create your first journal entry and set reminders.", "Help Center"),
        HelpEntry("feature-guides", "Feature Guides", "Deep dives on analytics, mood tracking, and export flows.", "Help Center"),
        HelpEntry("troubleshoot-sync", "Troubleshooting Sync", "Steps for resolving sync conflicts or offline edits.", "Help Center"),
        HelpEntry("privacy-security", "Privacy & Security", "Understand how data encryption and backups work.", "Help Center"),
        HelpEntry("walkthroughs", "Feature Walkthroughs", "Interactive demos that guide you through key workflows.", "Interactive Learning"),
        HelpEntry("video-tutorials", "Video Tutorials", "Short clips that show the app in action.", "Interactive Learning"),
        HelpEntry("practice-mode", "Practice Mode", "Enter a safe mode with sample data to practice journaling.", "Interactive Learning"),
        HelpEntry("chat", "Chat With Us", "Reach our support team from inside the app.", "Get in Touch"),
        HelpEntry("email", "Email Support", "Send us a detailed message—responses within 24 hours.", "Get in Touch"),
        HelpEntry("community", "Community Forum", "Connect with other writers for tips and encouragement.", "Get in Touch"),
        HelpEntry("feature-request", "Submit a Feature Request", "Share ideas for new capabilities.", "Feedback & Suggestions"),
        HelpEntry("report-bug", "Report a Bug", "Something broken? Let us know so we can fix it.", "Feedback & Suggestions"),
        HelpEntry("survey", "Take a Satisfaction Survey", "Tell us how we're doing and what to improve.", "Feedback & Suggestions")
    )

    private val _state = MutableStateFlow(
        HelpCenterUiState(
            sections = entries.groupBy { it.category }
                .map { (category, entries) -> HelpSection(category, entries) }
        )
    )
    val state: StateFlow<HelpCenterUiState> = _state

    fun updateQuery(query: String) {
        val trimmed = query.trim()
        _state.update { current ->
            val filtered = if (trimmed.isBlank()) emptyList() else {
                entries.filter { entry ->
                    entry.title.contains(trimmed, ignoreCase = true) ||
                        entry.description.contains(trimmed, ignoreCase = true)
                }
            }
            current.copy(
                query = trimmed,
                filteredEntries = filtered
            )
        }
    }
}
