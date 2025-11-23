package com.personaljournal.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personaljournal.domain.model.EntryFilter
import com.personaljournal.domain.model.MoodLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersBar(
    filter: EntryFilter,
    onToggleMood: (MoodLevel) -> Unit,
    onMediaToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MoodLevel.values().forEach { mood ->
            val selected = filter.moodLevels.contains(mood)
            AssistChip(
                onClick = { onToggleMood(mood) },
                label = { Text(mood.name.lowercase().replaceFirstChar { it.uppercase() }) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surface
                )
            )
        }
        val hasMedia = filter.hasMedia == true
        AssistChip(
            onClick = onMediaToggle,
            label = { Text("Media") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (hasMedia) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surface
            )
        )
    }
}
