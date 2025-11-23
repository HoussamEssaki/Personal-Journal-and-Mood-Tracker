package com.personaljournal.presentation.screens.mood

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personaljournal.domain.model.Mood
import com.personaljournal.presentation.viewmodel.EditorUiState
import com.personaljournal.presentation.viewmodel.EditorViewModel

private val emotionChips = listOf(
    "Happy", "Excited", "Grateful", "Motivated",
    "Calm", "Peaceful", "Frustrated", "Sad", "Anxious"
)

@Composable
fun DetailedMoodSelectorRoute(
    onDone: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DetailedMoodSelectorScreen(
        state = state,
        onMoodSelected = viewModel::selectMood,
        onToggleEmotion = viewModel::toggleEmotion,
        onToggleFactor = viewModel::toggleFactor,
        onNoteChanged = viewModel::updateContent,
        onDone = onDone
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailedMoodSelectorScreen(
    state: EditorUiState,
    onMoodSelected: (Mood) -> Unit,
    onToggleEmotion: (String) -> Unit,
    onToggleFactor: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onDone: () -> Unit
) {
    val peacefulActive = state.factors.contains("Peaceful")
    val lonelyActive = state.factors.contains("Lonely")
    var peacefulSlider by remember(state.factors) {
        mutableStateOf(if (peacefulActive) 0.8f else 0.2f)
    }
    var lonelySlider by remember(state.factors) {
        mutableStateOf(if (lonelyActive) 0.7f else 0.3f)
    }

    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("How are you feeling?", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = state.selectedMood?.label ?: "",
                color = MaterialTheme.colorScheme.primary
            )
        }
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Select your core mood", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.moods.forEach { mood ->
                        val selected = mood.id == state.selectedMood?.id
                        Surface(
                            shape = CircleShape,
                            tonalElevation = if (selected) 8.dp else 0.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable { onMoodSelected(mood) }
                        ) {
                            Text(
                                text = mood.label,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Text("Emotions", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    emotionChips.forEach { label ->
                        val selected = state.emotions.contains(label)
                        Surface(
                            shape = CircleShape,
                            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable { onToggleEmotion(label) }
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Text("Peaceful", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = peacefulSlider,
                    onValueChange = { value ->
                        peacefulSlider = value
                        val isActive = state.factors.contains("Peaceful")
                        if (value > 0.65f && !isActive) onToggleFactor("Peaceful")
                        if (value < 0.35f && isActive) onToggleFactor("Peaceful")
                    }
                )
                Text("Lonely", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = lonelySlider,
                    onValueChange = { value ->
                        lonelySlider = value
                        val isActive = state.factors.contains("Lonely")
                        if (value > 0.65f && !isActive) onToggleFactor("Lonely")
                        if (value < 0.35f && isActive) onToggleFactor("Lonely")
                    }
                )
                Text("Quick note", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = state.content,
                    onValueChange = onNoteChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add a quick note...") }
                )
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Mood Details")
                }
            }
        }
    }
}
