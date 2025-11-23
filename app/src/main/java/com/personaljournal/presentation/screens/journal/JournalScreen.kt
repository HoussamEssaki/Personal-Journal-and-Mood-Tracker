package com.personaljournal.presentation.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personaljournal.domain.model.EntryFilter
import com.personaljournal.domain.model.MoodLevel
import com.personaljournal.presentation.viewmodel.JournalUiState
import com.personaljournal.presentation.ui.components.EntryCard
import com.personaljournal.presentation.ui.components.FiltersBar
import com.personaljournal.presentation.ui.components.MoodCalendar
import com.personaljournal.presentation.viewmodel.JournalViewModel

@Composable
fun JournalRoute(
    onOpenEntry: (Long) -> Unit,
    viewModel: JournalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    JournalScreen(
        state = state,
        onFilterChanged = viewModel::updateFilter,
        onOpenEntry = onOpenEntry,
        onTogglePin = viewModel::togglePin
    )
}

@Composable
fun JournalScreen(
    state: JournalUiState,
    onFilterChanged: (EntryFilter) -> Unit,
    onOpenEntry: (Long) -> Unit,
    onTogglePin: (com.personaljournal.domain.model.JournalEntry) -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text(text = "Journal", style = MaterialTheme.typography.headlineSmall)
            var query by remember(state.filter.query) { mutableStateOf(state.filter.query.orEmpty()) }
            var startDate by remember(state.filter.startDateIso) { mutableStateOf(state.filter.startDateIso.orEmpty()) }
            var endDate by remember(state.filter.endDateIso) { mutableStateOf(state.filter.endDateIso.orEmpty()) }
            var tags by remember(state.filter.tagLabels) { mutableStateOf(state.filter.tagLabels.joinToString(",")) }
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    onFilterChanged(state.filter.copy(query = it))
                },
                label = { Text("Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = {
                        startDate = it
                        onFilterChanged(state.filter.copy(startDateIso = it.ifBlank { null }))
                    },
                    label = { Text("Start date (YYYY-MM-DD)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = {
                        endDate = it
                        onFilterChanged(state.filter.copy(endDateIso = it.ifBlank { null }))
                    },
                    label = { Text("End date (YYYY-MM-DD)") },
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = tags,
                onValueChange = {
                    tags = it
                    val parsed = it.split(",").mapNotNull { s -> s.trim().takeIf { t -> t.isNotEmpty() } }.toSet()
                    onFilterChanged(state.filter.copy(tagLabels = parsed))
                },
                label = { Text("Tags (comma-separated)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            Text(
                text = "Filters apply to the list and calendar below.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            FiltersBar(
                filter = state.filter,
                onToggleMood = { mood ->
                    val moods = state.filter.moodLevels.toMutableSet()
                    if (moods.contains(mood)) moods.remove(mood) else moods.add(mood)
                    onFilterChanged(state.filter.copy(moodLevels = moods))
                },
                onMediaToggle = {
                    val current = state.filter.hasMedia
                    onFilterChanged(state.filter.copy(hasMedia = !(current ?: false)))
                },
                modifier = Modifier.padding(top = 8.dp)
            )
            MoodCalendar(
                days = state.moodCalendar,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxSize()
            ) {
                if (state.entries.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("No entries yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Start a reflection from the dashboard to see your journal history here.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.entries) { entry ->
                            EntryCard(
                                entry = entry,
                                onClick = { onOpenEntry(entry.id) },
                                onTogglePin = { onTogglePin(entry) }
                            )
                        }
                    }
                }
            }
        }
    }
}
