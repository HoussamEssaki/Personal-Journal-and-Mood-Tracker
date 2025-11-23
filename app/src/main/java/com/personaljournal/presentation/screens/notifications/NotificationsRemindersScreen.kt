package com.personaljournal.presentation.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personaljournal.domain.model.NotificationEvent
import com.personaljournal.domain.model.NotificationStatus
import com.personaljournal.presentation.viewmodel.NotificationsUiState
import com.personaljournal.presentation.viewmodel.NotificationsViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun NotificationsRemindersRoute(
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NotificationsRemindersScreen(
        state = state,
        onToggle = viewModel::toggleReminder,
        onClearHistory = viewModel::clearHistory
    )
}

@Composable
fun NotificationsRemindersScreen(
    state: NotificationsUiState,
    onToggle: (String, Boolean) -> Unit,
    onClearHistory: () -> Unit
) {
    val background = Color(0xFF070E1B)
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val filters = listOf("All") + NotificationStatus.values().map { it.name.lowercase().replaceFirstChar { c -> c.titlecase() } }
    var selectedFilter by rememberSaveable { mutableStateOf("All") }
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    var historyLimit by rememberSaveable { mutableStateOf(50) }

    Column(
        modifier = Modifier
            .background(background)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Notifications & Reminders",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
        SegmentedTabs(selected = selectedTab, onSelect = { selectedTab = it })
        if (selectedTab == 0) {
            ReminderStatusBanner()
            state.toggles.forEach { toggle ->
                ReminderCard(
                    title = toggle.title,
                    subtitle = toggle.subtitle,
                    enabled = toggle.enabled,
                    onToggle = { onToggle(toggle.id, it) }
                )
            }
            CollapsibleCard("General Settings")
            CollapsibleCard("Smart Notifications")
        } else {
            Text(
                text = "History",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            FilterRow(filters = filters, selected = selectedFilter, onSelect = {
                selectedFilter = it
                historyLimit = 50
            })
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
            Text(
                text = "Recent notification events",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "Clear",
                color = Color(0xFFF97373),
                modifier = Modifier
                    .clickable { showClearDialog = true }
                    .semantics { contentDescription = "Clear notification history" },
                style = MaterialTheme.typography.labelLarge
            )
        }

            val filteredHistory = remember(state.history, selectedFilter) {
                val base = if (selectedFilter == "All") state.history
                else state.history.filter { it.status.name.equals(selectedFilter, ignoreCase = true) }
                base.sortedByDescending { it.timestamp }.take(500) // cap to prevent huge lists
            }

            state.history.firstOrNull()?.let { latest ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = Color(0xFF0E1729)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Last event", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(latest.title, color = Color.White)
                        Text(latest.status.name, color = Color(0xFF6E8EFF))
                        Text(formatEventTimestamp(latest), color = Color(0xFF8DA2C8))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Total events: ${state.history.size} (showing ${filteredHistory.size})",
                color = Color(0xFF8DA2C8),
                style = MaterialTheme.typography.bodySmall
            )

            if (filteredHistory.isEmpty()) {
                Text(
                    text = "No notification events yet.",
                    color = Color(0xFF8DA2C8)
                )
            } else {
                val limitedHistory = remember(filteredHistory, historyLimit) {
                    filteredHistory.take(historyLimit)
                }
                limitedHistory.firstOrNull { it.status == NotificationStatus.SCHEDULED }?.let { scheduled ->
                    Text(
                        text = "Scheduled: ${scheduled.title}",
                        color = Color(0xFF8DA2C8),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                limitedHistory.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF0E1729))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(formatEventTimestamp(item), color = Color(0xFF8DA2C8), modifier = Modifier.padding(top = 4.dp))
                            StatusPill(text = item.status.name)
                            Text(item.message, color = Color(0xFF8DA2C8), modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
                if (filteredHistory.size > historyLimit) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Show more (${historyLimit}/${filteredHistory.size})",
                        color = Color(0xFF6E8EFF),
                        modifier = Modifier
                            .clickable { historyLimit += 50 }
                            .semantics { contentDescription = "Show more history, showing $historyLimit of ${filteredHistory.size}" },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear history?") },
            text = { Text("This will remove all logged notification events.") },
            confirmButton = {
                Text(
                    text = "Clear",
                    color = Color(0xFFF97373),
                    modifier = Modifier.clickable {
                        onClearHistory()
                        showClearDialog = false
                    }
                )
            },
            dismissButton = {
                Text(
                    text = "Cancel",
                    color = Color.White,
                    modifier = Modifier.clickable { showClearDialog = false }
                )
            }
        )
    }
}

@Composable
private fun SegmentedTabs(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf("Settings", "History").forEachIndexed { index, label ->
            Card(
                shape = RoundedCornerShape(40.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = if (index == selected) Color(0xFF151F33) else Color.Transparent
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(index) }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    onToggle: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color(0xFF0E1729)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, color = Color.White)
                Text(subtitle, color = Color(0xFF8DA2C8))
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                modifier = Modifier.semantics {
                    contentDescription = "$title toggle ${if (enabled) "on" else "off"}"
                }
            )
        }
    }
}

@Composable
private fun CollapsibleCard(title: String) {
    val expanded = remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color(0xFF0E1729)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Color.White)
                Text(
                    text = if (expanded.value) "Hide" else "Show",
                    color = Color(0xFF6E8EFF),
                    modifier = Modifier.clickable { expanded.value = !expanded.value }
                )
            }
            if (expanded.value) {
                Text(
                    text = "Fine-tune notification sounds, priority levels, and batching preferences.",
                    modifier = Modifier.padding(top = 8.dp),
                    color = Color(0xFF8DA2C8)
                )
            }
        }
    }
}

@Composable
private fun ReminderStatusBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color(0xFF0E1729)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Reminder tips", color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(
                "Daily reminder uses WorkManager; recent runs appear in history when scheduled or delivered.",
                color = Color(0xFF8DA2C8)
            )
        }
    }
}

@Composable
private fun FilterRow(
    filters: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selected
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF151F33) else Color.Transparent,
                modifier = Modifier.clickable { onSelect(filter) }.semantics {
                    contentDescription = "Filter $filter ${if (isSelected) "selected" else "not selected"}"
                }
            ) {
                Text(
                    text = filter,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusPill(text: String) {
    val color = when {
        text.contains("Scheduled", ignoreCase = true) -> Color(0xFF4ADE80)
        text.contains("Delivered", ignoreCase = true) -> Color(0xFF6E8EFF)
        text.contains("Paused", ignoreCase = true) || text.contains("Cancelled", ignoreCase = true) -> Color(0xFFF97373)
        else -> Color(0xFF8DA2C8)
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = text,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun formatEventTimestamp(event: NotificationEvent): String {
    val local = event.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.month.name.lowercase().replaceFirstChar { it.titlecase() }} ${local.dayOfMonth}, ${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
}
