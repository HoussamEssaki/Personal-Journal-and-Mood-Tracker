package com.personaljournal.presentation.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personaljournal.domain.model.AchievementBadge
import com.personaljournal.domain.model.GoalProgress
import com.personaljournal.domain.model.HabitTracker
import com.personaljournal.presentation.viewmodel.GoalsUiState
import com.personaljournal.presentation.viewmodel.GoalsViewModel

private val BackgroundColor = Color(0xFF052410)

@Composable
fun GoalsHabitsRoute(
    viewModel: GoalsViewModel = hiltViewModel(),
    onReviewDashboard: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    GoalsHabitsScreen(
        state = state,
        onIncrementGoal = viewModel::incrementGoal,
        onToggleHabit = viewModel::toggleHabit,
        onAddGoal = { title, target, unit -> viewModel.addGoal(title, target, unit) },
        onAddHabit = { label -> viewModel.addHabit(label) },
        onReviewDashboard = onReviewDashboard
    )
}

@Composable
fun GoalsHabitsScreen(
    state: GoalsUiState,
    onIncrementGoal: (String) -> Unit,
    onToggleHabit: (String, Int) -> Unit,
    onAddGoal: (String, Int, String) -> Unit,
    onAddHabit: (String) -> Unit,
    onReviewDashboard: () -> Unit = {}
) {
    val hasData = state.goals.isNotEmpty() || state.habits.isNotEmpty()
    val newGoalTitle = rememberSaveable { mutableStateOf("") }
    val newGoalTarget = rememberSaveable { mutableStateOf("10") }
    val newGoalUnit = rememberSaveable { mutableStateOf("units") }
    val newHabit = rememberSaveable { mutableStateOf("") }
    val addError = remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .background(BackgroundColor)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "My Goals",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
        Surface(
            color = Color(0xFF0C2E19),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = spacedBy(6.dp)
            ) {
                Text(
                    text = "Daily actions",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "1) Tap \"Add\" on a goal to log progress\n2) Toggle the day cells to mark habits\n3) Check the dashboard for streaks and insights",
                    color = Color(0xFF9CE0AD),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C2E19)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = spacedBy(10.dp)
            ) {
                Text("Add a goal", color = Color.White, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = newGoalTitle.value,
                    onValueChange = { newGoalTitle.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Goal title") },
                    singleLine = true
                )
                Row(horizontalArrangement = spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newGoalTarget.value,
                        onValueChange = { newGoalTarget.value = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Target") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = newGoalUnit.value,
                        onValueChange = { newGoalUnit.value = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Unit") },
                        singleLine = true
                    )
                }
                OutlinedButton(
                    onClick = {
                        val title = newGoalTitle.value.trim()
                        val unit = newGoalUnit.value.trim().ifBlank { "units" }
                        val target = newGoalTarget.value.toIntOrNull()
                        if (title.isBlank() || target == null || target <= 0) {
                            addError.value = "Enter a title and a target > 0"
                        } else {
                            addError.value = null
                            onAddGoal(title, target, unit)
                            newGoalTitle.value = ""
                            newGoalTarget.value = "10"
                            newGoalUnit.value = unit
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4ADE80))
                ) {
                    Text("Save goal", color = Color(0xFF4ADE80))
                }
                if (addError.value != null) {
                    Text(addError.value!!, color = Color(0xFFFF8A80), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C2E19)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = spacedBy(10.dp)
            ) {
                Text("Add a habit", color = Color.White, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = newHabit.value,
                    onValueChange = { newHabit.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Habit name") },
                    singleLine = true
                )
                OutlinedButton(
                    onClick = {
                        val label = newHabit.value.trim()
                        if (label.isBlank()) {
                            addError.value = "Habit name required"
                        } else {
                            addError.value = null
                            onAddHabit(label)
                            newHabit.value = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4ADE80))
                ) {
                    Text("Save habit", color = Color(0xFF4ADE80))
                }
            }
        }
        if (!hasData) {
            EmptyStateCard()
        }
        WeeklySummaryCard(habits = state.habits)
        SummaryRow(state = state, onIncrementGoal = onIncrementGoal, onReviewDashboard = onReviewDashboard)
        state.goals.forEach { goal ->
            GoalCard(goal = goal, onIncrement = onIncrementGoal)
        }
        Text(
            text = "Weekly Habits • ${averageRate(state.habits)}% This Week",
            color = Color(0xFF7AE28A),
            style = MaterialTheme.typography.titleMedium
        )
        HabitGrid(
            habits = state.habits,
            onToggleHabit = onToggleHabit
        )
        Text(
            text = "My Achievements",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )
        AchievementRow(state.achievements)
    }
}

@Composable
private fun GoalCard(
    goal: GoalProgress,
    onIncrement: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0C2E19)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(goal.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Add",
                    modifier = Modifier
                        .border(1.dp, Color(0xFF4ADE80), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clickableNoRipple { onIncrement(goal.id) }
                        .semantics { contentDescription = "Add progress to ${goal.title}" },
                    color = Color(0xFF4ADE80),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Text(goal.statusLabel, color = Color(0xFF76E68E), modifier = Modifier.padding(top = 4.dp))
            LinearProgressIndicator(
                progress = { goal.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                color = Color(0xFF4ADE80),
                trackColor = Color(0xFF1A3F25)
            )
            Text(
                "Streak: ${goal.streakDays} days",
                color = Color(0xFFF9A825),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun HabitGrid(
    habits: List<HabitTracker>,
    onToggleHabit: (String, Int) -> Unit
) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0C2E19), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("", color = Color.White)
            days.forEach { day ->
                Text(
                    day,
                    color = Color(0xFF7AE28A),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        habits.forEach { habit ->
            HabitRow(habit = habit, onToggleHabit = onToggleHabit)
        }
    }
}

@Composable
private fun HabitRow(
    habit: HabitTracker,
    onToggleHabit: (String, Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(habit.label, color = Color.White, modifier = Modifier.padding(end = 8.dp))
        habit.completion.forEachIndexed { index, checked ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .clickableNoRipple { onToggleHabit(habit.id, index) }
                    .semantics {
                        contentDescription = "${habit.label} day ${index + 1} ${if (checked) "completed" else "not completed"}"
                    },
                color = if (checked) Color(0xFF4ADE80) else Color(0xFF1E4026),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (checked) "✓" else "",
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun AchievementRow(achievements: List<AchievementBadge>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        achievements.take(4).forEach { badge ->
            val color = if (badge.unlocked) Color(0xFF0C2E19) else Color(0xFF07150C)
            Surface(
                modifier = Modifier.weight(1f),
                color = color,
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 18.dp, horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge.label,
                        color = if (badge.unlocked) Color.White else Color(0xFF4B5E53),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun averageRate(habits: List<HabitTracker>): Int {
    if (habits.isEmpty()) return 0
    return habits.map { it.completionRate }.average().toInt()
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = MutableInteractionSource(),
            onClick = onClick
        )
    )

@Composable
private fun SummaryRow(
    state: GoalsUiState,
    onIncrementGoal: (String) -> Unit,
    onReviewDashboard: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C2E19)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = spacedBy(12.dp)
        ) {
            Text("Stay on track", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Log a win or mark a habit so progress shows on your dashboard.",
                color = Color(0xFF8DD6A5)
            )
            Row(horizontalArrangement = spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        state.goals.firstOrNull()?.let { onIncrementGoal(it.id) }
                    },
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4ADE80)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Update a goal", color = Color(0xFF4ADE80))
                }
                OutlinedButton(
                    onClick = onReviewDashboard,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Review dashboard")
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C2E19)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No goals or habits yet",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Add goals and habits from the dashboard card to track progress and feed your analytics.",
                color = Color(0xFF8BCFA0),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun WeeklySummaryCard(habits: List<HabitTracker>) {
    val avg = averageRate(habits)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C2E19)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = spacedBy(8.dp)
        ) {
            Text("This week", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Average habit completion: $avg%",
                color = Color(0xFF8DD6A5)
            )
            LinearProgressIndicator(
                progress = { avg / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF4ADE80),
                trackColor = Color(0xFF1A3F25)
            )
        }
    }
}
