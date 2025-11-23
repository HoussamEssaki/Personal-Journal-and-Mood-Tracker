package com.personaljournal.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personaljournal.domain.model.DensityMode
import com.personaljournal.domain.model.LayoutPreset
import com.personaljournal.domain.model.HabitTracker
import com.personaljournal.domain.model.GoalProgress
import com.personaljournal.domain.model.Mood
import com.personaljournal.domain.model.QuickCaptureTarget
import com.personaljournal.presentation.navigation.FeatureDestination
import com.personaljournal.presentation.navigation.FeatureDestinations
import com.personaljournal.presentation.viewmodel.DashboardUiState
import com.personaljournal.presentation.ui.components.EntryCard
import com.personaljournal.presentation.ui.components.MoodSelector
import com.personaljournal.presentation.ui.components.StatsChart
import com.personaljournal.presentation.viewmodel.DashboardViewModel

@Composable
fun DashboardRoute(
    onCreateEntry: (QuickCaptureTarget?) -> Unit,
    onOpenEntry: (Long) -> Unit,
    onOpenFeature: (FeatureDestination) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DashboardScreen(
        state = state,
        onCreateEntry = onCreateEntry,
        onOpenEntry = onOpenEntry,
        onMoodSelected = viewModel::selectMood,
        onOpenFeature = onOpenFeature,
        onOpenSettings = onOpenSettings
    )
}

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onCreateEntry: (QuickCaptureTarget?) -> Unit,
    onOpenEntry: (Long) -> Unit,
    onMoodSelected: (Mood) -> Unit,
    onOpenFeature: (FeatureDestination) -> Unit,
    onOpenSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(
            if (state.layout.densityMode == DensityMode.COMPACT) 12.dp else 18.dp
        )
    ) {
        val showPrompt = state.personal.showPrompts && state.prompt != null
        val sections = dashboardSections(state.layout.preset, showPrompt)
        sections.forEach { section ->
            when (section) {
                DashboardSection.HEADER -> {
                    item {
                        DashboardHeader(
                            onQuickCapture = { onCreateEntry(state.personal.quickCapture) },
                            streak = state.stats.activeStreakDays,
                            ctaLabel = quickCaptureCtaLabel(state.personal.quickCapture)
                        )
                    }
                    item {
                        MoreToolsCard(
                            onOpenFeature = onOpenFeature,
                            onOpenSettings = onOpenSettings
                        )
                    }
                }
                DashboardSection.MOOD -> {
                    item {
                        Text(
                            text = "How are you feeling today?",
                            style = MaterialTheme.typography.titleLarge
                        )
                        MoodSelector(
                            moods = state.moods,
                            selected = state.selectedMood,
                            onMoodSelected = onMoodSelected,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                DashboardSection.METRICS -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ElevatedCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { contentDescription = "Streak counter ${state.stats.activeStreakDays} days" }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Streak Counter", style = MaterialTheme.typography.labelLarge)
                                    Text(
                                        text = "${state.stats.activeStreakDays} Days",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            }
                            ElevatedCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics { contentDescription = "Entries this week ${state.entries.size}" }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Entries This Week", style = MaterialTheme.typography.labelLarge)
                                    Text(
                                        text = "${state.entries.size}",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            }
                        }
                    }
                }
                DashboardSection.GOALS -> {
                    item {
                        GoalsSummaryCard(
                            goals = state.goals,
                            habits = state.habits,
                            onOpenFeature = onOpenFeature
                        )
                    }
                }
                DashboardSection.TREND -> {
                    item {
                        Text(
                            text = "Your Week at a Glance",
                            style = MaterialTheme.typography.titleMedium
                        )
                        StatsChart(
                            points = state.stats.recentTrend,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .semantics {
                                    contentDescription = "Weekly mood chart, ${state.stats.recentTrend.size} points"
                                }
                        )
                    }
                }
                DashboardSection.PROMPT -> {
                    state.prompt?.let { prompt ->
                        item {
                            Card {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "Quick Insight", color = Color(0xFF3B873E))
                                    Text(
                                        text = prompt.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Text(
                                        text = prompt.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                DashboardSection.ENTRIES -> {
                    item {
                        Text(text = "Recent entries", style = MaterialTheme.typography.titleMedium)
                    }
                    if (state.entries.isEmpty()) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.large,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("No entries yet", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "Start a quick capture to build your streak.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(state.entries.take(5)) { entry ->
                            EntryCard(
                                entry = entry,
                                onClick = { onOpenEntry(entry.id) }
                            )
                        }
                    }
                }
                DashboardSection.QUICK_CAPTURE -> {
                    item {
                        QuickCaptureCard(
                            target = state.personal.quickCapture,
                            onQuickCapture = { onCreateEntry(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    onQuickCapture: () -> Unit,
    streak: Int,
    ctaLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(20.dp)
        ) {
            Text("Tuesday, Oct 26", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Welcome back!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Current Streak", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "$streak days",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(onClick = onQuickCapture) {
                    Text(ctaLabel)
                }
            }
        }
    }
}

@Composable
private fun QuickCaptureCard(
    target: QuickCaptureTarget,
    onQuickCapture: (QuickCaptureTarget) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Quick capture ${quickCaptureDescription(target)}"
            },
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Quick capture", style = MaterialTheme.typography.titleMedium)
            Text(
                text = quickCaptureDescription(target),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = { onQuickCapture(target) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(quickCaptureCtaLabel(target))
            }
        }
    }
}

private enum class DashboardSection {
    HEADER, MOOD, METRICS, GOALS, TREND, PROMPT, ENTRIES, QUICK_CAPTURE
}

@Composable
private fun MoreToolsCard(
    onOpenFeature: (FeatureDestination) -> Unit,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("More tools", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Jump to analytics, export/backup, personalization, notifications, calendar, search, and help.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val entries: List<FeatureDestination> = listOf(
                FeatureDestination(
                    route = FeatureDestinations.TrendsInsights,
                    title = "Analytics & Insights",
                    description = "Trends, heatmaps, correlations"
                ),
                FeatureDestination(
                    route = FeatureDestinations.ExportShare,
                    title = "Export & Backup Center",
                    description = "PDF/CSV/JSON and backups"
                ),
                FeatureDestination(
                    route = FeatureDestinations.Notifications,
                    title = "Notifications & History",
                    description = "Reminders, history, filters"
                ),
                FeatureDestination(
                    route = FeatureDestinations.Personalization,
                    title = "Personalization & Themes",
                    description = "Colors, layout, font size"
                ),
                FeatureDestination(
                    route = FeatureDestinations.CalendarView,
                    title = "Calendar View",
                    description = "Browse entries on a calendar"
                ),
                FeatureDestination(
                    route = FeatureDestinations.SearchFilters,
                    title = "Search & Filters",
                    description = "Find entries by mood, tag, or date"
                ),
                FeatureDestination(
                    route = FeatureDestinations.GoalsHabits,
                    title = "Goals & Habits",
                    description = "Track progress and habits"
                ),
                FeatureDestination(
                    route = FeatureDestinations.SupportHelp,
                    title = "Help & Support",
                    description = "Guides, FAQ, and contact"
                )
            )
            entries.forEach { destination ->
                Button(
                    onClick = { onOpenFeature(destination) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Open ${destination.title}" }
                ) {
                    Text(destination.title)
                }
            }
            HorizontalDivider()
            Button(
                onClick = onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Open Settings" }
            ) { Text("Settings") }
        }
    }
}

private fun dashboardSections(
    preset: LayoutPreset,
    includePrompt: Boolean
): List<DashboardSection> {
    val base = when (preset) {
        LayoutPreset.BALANCED -> listOf(
            DashboardSection.HEADER,
            DashboardSection.MOOD,
            DashboardSection.METRICS,
            DashboardSection.GOALS,
            DashboardSection.TREND,
            DashboardSection.PROMPT,
            DashboardSection.ENTRIES,
            DashboardSection.QUICK_CAPTURE
        )
        LayoutPreset.JOURNAL_FIRST -> listOf(
            DashboardSection.HEADER,
            DashboardSection.QUICK_CAPTURE,
            DashboardSection.ENTRIES,
            DashboardSection.MOOD,
            DashboardSection.PROMPT,
            DashboardSection.METRICS,
            DashboardSection.GOALS,
            DashboardSection.TREND
        )
        LayoutPreset.INSIGHTS_FOCUS -> listOf(
            DashboardSection.HEADER,
            DashboardSection.METRICS,
            DashboardSection.GOALS,
            DashboardSection.TREND,
            DashboardSection.PROMPT,
            DashboardSection.MOOD,
            DashboardSection.ENTRIES,
            DashboardSection.QUICK_CAPTURE
        )
    }
    return if (includePrompt) base else base.filterNot { it == DashboardSection.PROMPT }
}

private fun quickCaptureCtaLabel(target: QuickCaptureTarget): String = when (target) {
    QuickCaptureTarget.REFLECTION -> "Start reflection"
    QuickCaptureTarget.GRATITUDE -> "Log gratitude"
    QuickCaptureTarget.AUDIO -> "Record voice note"
}

private fun quickCaptureDescription(target: QuickCaptureTarget): String = when (target) {
    QuickCaptureTarget.REFLECTION -> "Open the journaling editor focused on mood + free writing."
    QuickCaptureTarget.GRATITUDE -> "Jump into a gratitude template with guiding prompts."
    QuickCaptureTarget.AUDIO -> "Launch the editor ready for attaching an audio entry."
}

@Composable
private fun GoalsSummaryCard(
    goals: List<GoalProgress>,
    habits: List<HabitTracker>,
    onOpenFeature: (FeatureDestination) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Goals & Habits", style = MaterialTheme.typography.titleMedium)
            val avgGoalProgress = goals.map { it.progressFraction }.takeIf { it.isNotEmpty() }?.average()
            val avgHabitCompletion = habits.map { it.completionRate }.takeIf { it.isNotEmpty() }?.average()
            if (avgGoalProgress != null || avgHabitCompletion != null) {
                val goalPct = avgGoalProgress?.let { (it * 100).toInt() }
                val habitPct = avgHabitCompletion?.toInt()
                val summary = buildString {
                    if (goalPct != null) append("Avg goal progress $goalPct%")
                    if (goalPct != null && habitPct != null) append(" • ")
                    if (habitPct != null) append("Avg habits $habitPct%")
                }
                Text(
                    text = summary,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (goals.isEmpty() && habits.isEmpty()) {
                Text(
                    text = "Add a goal or habit to keep progress visible here.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            goals.take(2).forEach { goal ->
                GoalRow(goal = goal)
            }

            if (habits.isNotEmpty() && goals.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            habits.take(2).forEach { habit ->
                HabitRow(habit = habit)
            }

            Button(
                onClick = {
                    onOpenFeature(
                        FeatureDestination(
                            FeatureDestinations.GoalsHabits,
                            "Goals & Habits",
                            "Track streaks and habits"
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Goals & Habits")
            }
        }
    }
}

@Composable
private fun GoalRow(goal: GoalProgress) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Goal ${goal.title} ${goal.statusLabel}"
            },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(goal.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(goal.statusLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress = { goal.progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Progress ${goal.statusLabel}" }
        )
    }
}

@Composable
private fun HabitRow(habit: HabitTracker) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Habit ${habit.label} ${habit.completionRate} percent this week"
            },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(habit.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text("${habit.completionRate}% this week", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress = { (habit.completionRate / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Completion ${habit.completionRate} percent" }
        )
    }
}
