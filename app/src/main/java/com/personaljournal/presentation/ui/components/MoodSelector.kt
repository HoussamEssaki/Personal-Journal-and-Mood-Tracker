package com.personaljournal.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.personaljournal.domain.model.Mood

@Composable
fun MoodSelector(
    moods: List<Mood>,
    selected: Mood?,
    onMoodSelected: (Mood) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        moods.forEach { mood ->
            val isSelected = selected?.id == mood.id
            Text(
                text = mood.emoji,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else Color.Transparent
                    )
                    .clickable { onMoodSelected(mood) }
                    .padding(12.dp)
            )
        }
    }
}
