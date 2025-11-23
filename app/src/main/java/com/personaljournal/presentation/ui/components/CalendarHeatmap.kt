package com.personaljournal.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.personaljournal.domain.model.MoodLevel

@Composable
fun MoodCalendar(
    days: Map<Int, MoodLevel>,
    modifier: Modifier = Modifier,
    daysInPeriod: Int = 31
) {
    val maxDay = daysInPeriod.coerceAtLeast(1)
    val items = (1..maxDay).map { it to days[it] }
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier
            .heightIn(max = 240.dp)
            .semantics {
                contentDescription = "Mood calendar with $maxDay days"
            }
    ) {
        items(items) { (day, mood) ->
            Canvas(
                modifier = Modifier
                    .padding(4.dp)
                    .size(32.dp)
            ) {
                drawCircle(
                    color = mood.toColor(),
                    radius = size.minDimension / 2
                )
            }
        }
    }
}

private fun MoodLevel?.toColor(): Color = when (this) {
    MoodLevel.EXCELLENT -> Color(0xFFF5A623)
    MoodLevel.GOOD -> Color(0xFF7ED321)
    MoodLevel.NEUTRAL -> Color(0xFF9B9B9B)
    MoodLevel.POOR -> Color(0xFFF15A29)
    MoodLevel.TERRIBLE -> Color(0xFFD0021B)
    null -> Color(0xFF1F2A33)
}
