package com.personaljournal.presentation.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.model.MoodLevel
import com.personaljournal.presentation.viewmodel.SearchUiState
import com.personaljournal.presentation.viewmodel.SearchViewModel

@Composable
fun SearchFiltersRoute(
    onOpenEntry: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchFiltersScreen(
        state = state,
        onQueryChange = viewModel::updateQuery,
        onToggleMood = viewModel::toggleMood,
        onToggleTag = viewModel::toggleTag,
        onToggleMedia = viewModel::toggleMediaFilter,
        onClearFilters = viewModel::clearFilters,
        onOpenEntry = onOpenEntry
    )
}

@Composable
fun SearchFiltersScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onToggleMood: (MoodLevel) -> Unit,
    onToggleTag: (String) -> Unit,
    onToggleMedia: () -> Unit,
    onClearFilters: () -> Unit,
    onOpenEntry: (Long) -> Unit
) {
    val background = Color(0xFF050C18)
    Column(
        modifier = Modifier
            .background(background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Search Journal",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search for memories, moods, people...") }
        )
        FilterChipsSection(state, onToggleMood, onToggleTag, onToggleMedia, onClearFilters)
        Text(
            text = "${state.results.size} entries found",
            color = Color(0xFF9AB1D1)
        )
        state.results.forEach { entry ->
            SearchResultCard(
                entry = entry,
                highlight = state.query,
                onOpenEntry = onOpenEntry
            )
        }
        if (state.results.isEmpty()) {
            Surface(
                tonalElevation = 0.dp,
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No Entries Found", color = Color(0xFF6B7BA0))
                    Text(
                        "Try adjusting your search or filters to find what you're looking for.",
                        color = Color(0xFF6B7BA0),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipsSection(
    state: SearchUiState,
    onToggleMood: (MoodLevel) -> Unit,
    onToggleTag: (String) -> Unit,
    onToggleMedia: () -> Unit,
    onClearFilters: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filters", color = Color.White)
            Text(
                text = "Clear all",
                color = Color(0xFF6E8EFF),
                modifier = Modifier
                    .clickableNoRipple { onClearFilters() }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.moods.take(5).forEach { mood ->
                AssistChip(
                    onClick = { onToggleMood(mood.level) },
                    label = { Text(mood.label) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (state.selectedMoods.contains(mood.level)) Color(0xFF13213A) else Color.Transparent,
                        labelColor = Color.White
                    )
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.tags.take(6).forEach { tag ->
                AssistChip(
                    onClick = { onToggleTag(tag.label) },
                    label = { Text(tag.label) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (state.selectedTags.contains(tag.label)) Color(0xFF1C2B45) else Color.Transparent,
                        labelColor = Color.White
                    )
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Has media attachments", color = Color.White)
            Switch(
                checked = state.includeMediaOnly,
                onCheckedChange = { onToggleMedia() }
            )
        }
    }
}

@Composable
private fun SearchResultCard(
    entry: JournalEntry,
    highlight: String,
    onOpenEntry: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFF0D172A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(entry.createdAt.toString().take(10), color = Color.White)
            Text("•  ${entry.mood.label}", color = Color(0xFF8FB4FF))
            Text(
                text = highlightSnippet(entry.content, highlight),
                modifier = Modifier.padding(top = 8.dp),
                color = Color.White
            )
            Text(
                text = "View Entry",
                color = Color(0xFF6E8EFF),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickableNoRipple { onOpenEntry(entry.id) }
            )
        }
    }
}

private fun highlightSnippet(text: String, query: String) = buildAnnotatedString {
    val keyword = query.lowercase().takeIf { it.isNotBlank() } ?: return@buildAnnotatedString append(text)
    val lowercase = text.lowercase()
    var start = 0
    while (start < text.length) {
        val index = lowercase.indexOf(keyword, startIndex = start)
        if (index == -1) {
            append(text.substring(start))
            break
        }
        append(text.substring(start, index))
        withStyle(SpanStyle(color = Color(0xFF214DFF), fontWeight = FontWeight.Bold)) {
            append(text.substring(index, index + keyword.length))
        }
        start = index + keyword.length
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(
        clickable(
            indication = null,
            interactionSource = MutableInteractionSource(),
            onClick = onClick
        )
    )
