package com.personaljournal.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personaljournal.domain.model.JournalEntry
import com.personaljournal.domain.model.MoodLevel
import com.personaljournal.presentation.viewmodel.CalendarDayUi
import com.personaljournal.presentation.viewmodel.CalendarUiState
import com.personaljournal.presentation.viewmodel.CalendarViewModel
import java.time.Instant as JavaInstant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
private val selectedDateFormatter =
    DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
private val entryTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

@Composable
fun CalendarViewRoute(
    onViewEntry: (Long) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CalendarViewScreen(
        state = state,
        onPreviousMonth = viewModel::moveToPreviousMonth,
        onNextMonth = viewModel::moveToNextMonth,
        onSelectDay = viewModel::onSelectDay,
        onViewEntry = onViewEntry
    )
}

@Composable
fun CalendarViewScreen(
    state: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDay: (LocalDate) -> Unit,
    onViewEntry: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderRow(
            label = state.monthLabel,
            onPrevious = onPreviousMonth,
            onNext = onNextMonth
        )
        DayOfWeekRow()
        state.weeks.forEach { week ->
            WeekRow(
                days = week,
                selected = state.selectedDate,
                onSelect = onSelectDay
            )
        }
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = state.selectedDate?.format(selectedDateFormatter) ?: "",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (state.selectedEntries.isEmpty()) {
                    Text(
                        text = "No entries yet. Tap a day with a dot to view details.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    state.selectedEntries.forEach { entry ->
                        EntryPreview(
                            entry = entry,
                            onViewEntry = onViewEntry
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderRow(
    label: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = "Previous month")
        }
        Text(label, style = MaterialTheme.typography.headlineSmall)
        IconButton(onClick = onNext) {
            Icon(Icons.Outlined.ArrowForward, contentDescription = "Next month")
        }
    }
}

@Composable
private fun DayOfWeekRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        dayLabels.forEach {
            Text(
                text = it,
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeekRow(
    days: List<CalendarDayUi>,
    selected: LocalDate?,
    onSelect: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { day ->
            CalendarDayCell(
                day = day,
                isSelected = day.date != null && day.date == selected,
                onSelect = onSelect
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDayUi,
    isSelected: Boolean,
    onSelect: (LocalDate) -> Unit
) {
    if (day.date == null) {
        Spacer(modifier = Modifier.width(40.dp))
        return
    }
    val indicatorColor = day.moodLevel.toMoodColor()
    Column(
        modifier = Modifier
            .width(40.dp)
            .padding(vertical = 6.dp)
            .clip(CircleShape)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onSelect(day.date) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        if (day.entries.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
        }
    }
}

@Composable
private fun EntryPreview(
    entry: JournalEntry,
    onViewEntry: (Long) -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(entry.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = entry.content.take(140),
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.formatAsTime(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "View Entry",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onViewEntry(entry.id) }
                )
            }
        }
    }
}

private fun MoodLevel?.toMoodColor(): Color = when (this) {
    MoodLevel.EXCELLENT -> Color(0xFFFFC542)
    MoodLevel.GOOD -> Color(0xFF3DD598)
    MoodLevel.NEUTRAL -> Color(0xFFA0AEC0)
    MoodLevel.POOR -> Color(0xFFF88379)
    MoodLevel.TERRIBLE -> Color(0xFFE74362)
    null -> Color.Transparent
}

private fun JournalEntry.formatAsTime(): String {
    val zoned = JavaInstant.ofEpochMilli(createdAt.toEpochMilliseconds())
        .atZone(ZoneId.systemDefault())
    return entryTimeFormatter.format(zoned)
}
