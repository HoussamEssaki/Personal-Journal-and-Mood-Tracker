package com.personaljournal.presentation.screens.analytics

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personaljournal.BuildConfig
import com.personaljournal.domain.model.ExportFormat
import com.personaljournal.domain.model.MoodLevel
import com.personaljournal.domain.model.MoodTrendPoint
import com.personaljournal.domain.model.StatsSnapshot
import com.personaljournal.domain.model.TrendDirection
import com.personaljournal.presentation.ui.components.MoodCalendar
import com.personaljournal.presentation.ui.components.StatsChart
import com.personaljournal.presentation.viewmodel.AnalyticsUiState
import com.personaljournal.presentation.viewmodel.AnalyticsViewModel
import java.io.File
import kotlin.math.max
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AnalyticsRoute(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingAction by rememberSaveable { mutableStateOf<ExportAction?>(null) }
    var pendingFormat by rememberSaveable { mutableStateOf<ExportFormat?>(null) }
    var saveRequest by rememberSaveable { mutableStateOf<ExportSaveRequest?>(null) }

    val pdfSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val request = saveRequest
        if (uri != null && request?.file != null) {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                request.file.inputStream().use { it.copyTo(out) }
            }
        }
        saveRequest = null
        viewModel.clearExportStatus()
        pendingAction = null
    }
    val csvSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val request = saveRequest
        if (uri != null && request?.file != null) {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                request.file.inputStream().use { it.copyTo(out) }
            }
        }
        saveRequest = null
        viewModel.clearExportStatus()
        pendingAction = null
    }
    val jsonSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val request = saveRequest
        if (uri != null && request?.file != null) {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                request.file.inputStream().use { it.copyTo(out) }
            }
        }
        saveRequest = null
        viewModel.clearExportStatus()
        pendingAction = null
    }

    LaunchedEffect(state.exportFile, pendingAction, pendingFormat) {
        val file = state.exportFile
        val format = pendingFormat
        when {
            file != null && pendingAction == ExportAction.SHARE && format != null -> {
                shareExport(context, file, format)
                viewModel.clearExportStatus()
                pendingAction = null
                pendingFormat = null
            }
            file != null && pendingAction == ExportAction.SAVE && format != null -> {
                saveRequest = ExportSaveRequest(file, format)
                val suggestedName = when (format) {
                    ExportFormat.PDF -> "journal-analytics.pdf"
                    ExportFormat.CSV -> "journal-analytics.csv"
                    ExportFormat.JSON -> "journal-analytics.json"
                }
                when (format) {
                    ExportFormat.PDF -> pdfSaveLauncher.launch(suggestedName)
                    ExportFormat.CSV -> csvSaveLauncher.launch(suggestedName)
                    ExportFormat.JSON -> jsonSaveLauncher.launch(suggestedName)
                }
                pendingFormat = null
            }
        }
    }

    AnalyticsScreen(
        state = state,
        onExport = { format, action ->
            pendingFormat = format
            pendingAction = action
            viewModel.export(format, includeMedia = true)
        }
    )
}

@Composable
fun AnalyticsScreen(
    state: AnalyticsUiState,
    onExport: (ExportFormat, ExportAction) -> Unit
) {
    var showExportPickerFor by rememberSaveable { mutableStateOf<ExportFormat?>(null) }
    var selectedRange by rememberSaveable { mutableStateOf(TrendRange.MONTH) }
    val filteredTrend = remember(state.stats.recentTrend, selectedRange) {
        when (selectedRange) {
            TrendRange.WEEK -> state.stats.recentTrend.takeLast(7)
            TrendRange.MONTH -> state.stats.recentTrend.takeLast(30)
            TrendRange.QUARTER -> state.stats.recentTrend.takeLast(90)
            TrendRange.ALL -> state.stats.recentTrend
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Your Analytics", style = MaterialTheme.typography.headlineSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsStat("Current Streak", "${state.stats.activeStreakDays} days")
            AnalyticsStat("Avg Mood", formatMoodAverage(state.stats.averageMoodLevel))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsStat("Common Mood", friendlyMood(state.stats.mostCommonMood))
            AnalyticsStat("Total Entries", "${state.stats.totalEntries}")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsStat("Prompts Completed", "${state.stats.promptsCompleted}")
            AnalyticsStat("Correlations Found", "${state.stats.correlations.size}")
        }
        RangeChips(selected = selectedRange, onSelect = { selectedRange = it })
        HeatmapCard(points = filteredTrend, label = selectedRange.label)
        YearHeatmapCard(points = state.stats.recentTrend)
        MoodDeltaCard(points = filteredTrend)
        PeriodComparisonCard(points = filteredTrend)
        WeeklyBreakdownCard(points = filteredTrend)
        MoodDistributionCard(points = filteredTrend)
        CorrelationsCard(insights = state.stats.correlations)
        CorrelationSplitCard(insights = state.stats.correlations)
        YearOverviewCard(points = state.stats.recentTrend)
        TopFactorsCard(insights = state.stats.correlations)
        TabRow()
        StatsChart(
            points = filteredTrend,
            modifier = Modifier
                .padding(top = 8.dp)
                .semantics {
                    contentDescription = "Mood trend chart for ${selectedRange.label}, ${filteredTrend.size} points"
                }
        )
        ActivityPatternsSection(stats = state.stats)
        InsightsSummaryCard(state.stats.correlations)
        AchievementsRow()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { showExportPickerFor = ExportFormat.PDF },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Export analytics as PDF or share" }
            ) {
                Text("Export PDF / Share")
            }
            Button(
                onClick = { showExportPickerFor = ExportFormat.CSV },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Export analytics as CSV or share" }
            ) {
                Text("Export CSV / Share")
            }
            Button(
                onClick = { showExportPickerFor = ExportFormat.JSON },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Export analytics as JSON or share" }
            ) {
                Text("Export JSON / Share")
            }
        }
        state.exportFile?.let {
            Text(
                text = "Export ready: ${it.name}",
                color = MaterialTheme.colorScheme.primary
            )
        }
        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    showExportPickerFor?.let { format ->
        ExportActionDialog(
            format = format,
            onDismiss = { showExportPickerFor = null },
            onSelect = { action ->
                showExportPickerFor = null
                onExport(format, action)
            }
        )
    }
}

@Composable
private fun RowScope.AnalyticsStat(title: String, value: String) {
    Card(
        modifier = Modifier
            .weight(1f)
            .semantics { contentDescription = "$title $value" }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun TabRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf("Mood Trend", "Activities", "Patterns").forEachIndexed { index, label ->
            Surface(
                shape = CircleShape,
                tonalElevation = if (index == 0) 6.dp else 0.dp,
                color = if (index == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = "$label tab ${if (index == 0) "selected" else "not selected"}"
                    }
            ) {
                Text(
                    text = label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ActivityPatternsSection(stats: StatsSnapshot) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Activity", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total entries")
                    Text("${stats.totalEntries}")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Prompts completed")
                    Text("${stats.promptsCompleted}")
                }
            }
        }
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Patterns", style = MaterialTheme.typography.titleMedium)
                val positives = stats.correlations.filter { it.correlation > 0.1 }
                val negatives = stats.correlations.filter { it.correlation < -0.1 }
                if (positives.isEmpty() && negatives.isEmpty()) {
                    Text(
                        text = "No patterns detected yet. Keep journaling to see correlations.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    if (positives.isNotEmpty()) {
                        Text("Helps mood", style = MaterialTheme.typography.titleSmall)
                        positives.take(3).forEach { insight ->
                            Text(
                                text = "+ ${insight.factor} (${String.format("%.2f", insight.correlation)})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (negatives.isNotEmpty()) {
                        Text("Hurts mood", style = MaterialTheme.typography.titleSmall)
                        negatives.take(3).forEach { insight ->
                            Text(
                                text = "- ${insight.factor} (${String.format("%.2f", insight.correlation)})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportActionDialog(
    format: ExportFormat,
    onDismiss: () -> Unit,
    onSelect: (ExportAction) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export ${format.name}") },
        text = { Text("Choose whether to share now or save a copy to your device.") },
        confirmButton = {
            TextButton(onClick = { onSelect(ExportAction.SHARE) }) {
                Text("Share")
            }
        },
        dismissButton = {
            TextButton(onClick = { onSelect(ExportAction.SAVE) }) {
                Text("Save to device")
            }
        }
    )
}

enum class ExportAction { SHARE, SAVE }

private data class ExportSaveRequest(val file: File, val format: ExportFormat)

private fun shareExport(context: Context, file: File, format: ExportFormat) {
    if (!file.exists()) return
    val mimeType = when (format) {
        ExportFormat.PDF -> "application/pdf"
        ExportFormat.CSV -> "text/csv"
        ExportFormat.JSON -> "application/json"
    }
    val uri = FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileprovider",
        file
    )
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(Intent.EXTRA_TEXT, "Shared from Personal Journal")
    }
    try {
        context.startActivity(Intent.createChooser(shareIntent, "Share export"))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No compatible app found to share the file.", Toast.LENGTH_SHORT).show()
    }
}

private fun friendlyMood(mood: MoodLevel): String = when (mood) {
    MoodLevel.EXCELLENT -> "Excellent"
    MoodLevel.GOOD -> "Good"
    MoodLevel.NEUTRAL -> "Neutral"
    MoodLevel.POOR -> "Poor"
    MoodLevel.TERRIBLE -> "Terrible"
}

private fun formatMoodAverage(value: Double): String = when {
    value >= 4 -> "Very positive"
    value >= 3 -> "Positive"
    value >= 2 -> "Mixed"
    value >= 1 -> "Low"
    else -> "Very low"
}

@Composable
private fun MoodDeltaCard(points: List<MoodTrendPoint>) {
    if (points.size < 2) return
    val first = points.first()
    val last = points.last()
    val delta = last.averageScore - first.averageScore
    val direction = when {
        delta > 0.05 -> "up"
        delta < -0.05 -> "down"
        else -> "flat"
    }
    val color = when {
        delta > 0 -> MaterialTheme.colorScheme.primary
        delta < 0 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val formattedDelta = String.format("%+.2f", delta)
    Card {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Recent Mood Change", style = MaterialTheme.typography.titleMedium)
            Text("$direction over the recorded period ($formattedDelta)", color = color)
            Text(
                "First: ${formatTrendPoint(first)}, Latest: ${formatTrendPoint(last)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatTrendPoint(point: MoodTrendPoint): String {
    val date = point.date.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.month.name.lowercase().replaceFirstChar { it.titlecase() }} ${date.dayOfMonth} (${String.format("%.2f", point.averageScore)})"
}

@Composable
private fun CorrelationsCard(insights: List<com.personaljournal.domain.model.CorrelationInsight>) {
    Card {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Correlations", style = MaterialTheme.typography.titleMedium)
            if (insights.isEmpty()) {
                Text(
                    text = "No patterns detected yet. Keep journaling to see correlations.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                insights.take(5).forEach { insight ->
                    val direction = when (insight.trend) {
                        TrendDirection.UP -> "improves mood"
                        TrendDirection.DOWN -> "hurts mood"
                        TrendDirection.STABLE -> "has neutral impact"
                    }
                    Text(
                        text = "${insight.factor}: ${String.format("%.2f", insight.correlation)} • $direction",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (insights.size > 5) {
                    Text(
                        text = "+${insights.size - 5} more",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun CorrelationSplitCard(insights: List<com.personaljournal.domain.model.CorrelationInsight>) {
    val positive = insights.filter { it.correlation > 0.1 }
    val negative = insights.filter { it.correlation < -0.1 }
    if (positive.isEmpty() && negative.isEmpty()) return
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Helps mood", style = MaterialTheme.typography.titleSmall)
                if (positive.isEmpty()) {
                    Text(
                        "No positive factors yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    positive.take(3).forEach { insight ->
                        Text(
                            "+ ${insight.factor} (${String.format("%.2f", insight.correlation)})",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Hurts mood", style = MaterialTheme.typography.titleSmall)
                if (negative.isEmpty()) {
                    Text(
                        "No negative factors yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    negative.take(3).forEach { insight ->
                        Text(
                            "- ${insight.factor} (${String.format("%.2f", insight.correlation)})",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightsSummaryCard(correlations: List<com.personaljournal.domain.model.CorrelationInsight>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Your Insights", style = MaterialTheme.typography.titleMedium)
            if (correlations.isEmpty()) {
                Text(
                    "Keep logging entries to unlock personalized insights.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }
            val topPositive = correlations.filter { it.correlation > 0.1 }.maxByOrNull { it.correlation }
            val topNegative = correlations.filter { it.correlation < -0.1 }.minByOrNull { it.correlation }
            topPositive?.let {
                Text(
                    "Most helpful: ${it.factor} (boosts mood ${String.format("%.2f", it.correlation)})",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            topNegative?.let {
                Text(
                    "Most harmful: ${it.factor} (lowers mood ${String.format("%.2f", it.correlation)})",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (topPositive == null && topNegative == null) {
                Text(
                    "No strong positive or negative factors yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TopFactorsCard(insights: List<com.personaljournal.domain.model.CorrelationInsight>) {
    if (insights.isEmpty()) return
    val topPositive = insights.filter { it.correlation > 0.1 }.sortedByDescending { it.correlation }.take(3)
    val topNegative = insights.filter { it.correlation < -0.1 }.sortedBy { it.correlation }.take(3)
    if (topPositive.isEmpty() && topNegative.isEmpty()) return
    Card {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Top factors", style = MaterialTheme.typography.titleMedium)
            if (topPositive.isNotEmpty()) {
                Text("Boosts mood", style = MaterialTheme.typography.titleSmall)
                topPositive.forEach {
                    Text(
                        "+ ${it.factor} (${String.format("%.2f", it.correlation)})",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (topNegative.isNotEmpty()) {
                Text("Lowers mood", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 4.dp))
                topNegative.forEach {
                    Text(
                        "- ${it.factor} (${String.format("%.2f", it.correlation)})",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodComparisonCard(points: List<MoodTrendPoint>) {
    if (points.size < 4) return
    val mid = max(1, points.size / 2)
    val firstAvg = points.take(mid).map { it.averageScore }.average()
    val recentAvg = points.takeLast(mid).map { it.averageScore }.average()
    val delta = recentAvg - firstAvg
    val trend = when {
        delta > 0.05 -> "Recent period is higher"
        delta < -0.05 -> "Recent period is lower"
        else -> "Recent period is similar"
    }
    Card {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Period comparison", style = MaterialTheme.typography.titleMedium)
            Text(
                "$trend (${String.format("%.2f", firstAvg)} -> ${String.format("%.2f", recentAvg)})",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AchievementsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("30-Day Streak!", "Journalist", "Explorer", "Mood Sage").forEach { label ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 4.dp),
                tonalElevation = 2.dp,
                shape = CircleShape
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(vertical = 12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun WeeklyBreakdownCard(points: List<MoodTrendPoint>) {
    if (points.isEmpty()) return
    val grouped = points.groupBy {
        it.date.toLocalDateTime(TimeZone.currentSystemDefault()).date.dayOfWeek
    }
    Card {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Weekday mood averages", style = MaterialTheme.typography.titleMedium)
            grouped.entries.sortedBy { it.key.ordinal }.forEach { (day, items) ->
                val avg = if (items.isNotEmpty()) items.map { pt -> pt.averageScore }.average() else 0.0
                Text("${day.name.lowercase().replaceFirstChar { it.titlecase() }}: ${String.format("%.2f", avg)}")
            }
        }
    }
}

@Composable
private fun MoodDistributionCard(points: List<MoodTrendPoint>) {
    if (points.isEmpty()) return
    val total = points.size.toDouble().coerceAtLeast(1.0)
    val counts = points.groupBy { it.averageScore.toMoodLevel() }
    Card {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Mood distribution", style = MaterialTheme.typography.titleMedium)
            MoodLevel.values().forEach { level ->
                val count = counts[level]?.size ?: 0
                val percent = (count / total) * 100
                Text(
                    "${friendlyMood(level)}: $count (${String.format("%.0f", percent)}%)",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun HeatmapCard(points: List<MoodTrendPoint>, label: String) {
    if (points.isEmpty()) return
    val window = if (points.size > 31) points.takeLast(31) else points
    val dayMap: Map<Int, MoodLevel> = window.associate { pt ->
        val day = pt.date.toLocalDateTime(TimeZone.currentSystemDefault()).date.dayOfMonth
        day to pt.averageScore.toMoodLevel()
    }
    Card {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .semantics {
                    contentDescription = "Heatmap (${label}), ${points.size} points"
                },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val header = if (points.size > 31) "Heatmap (last 31 days)" else "Heatmap ($label)"
            Text(header, style = MaterialTheme.typography.titleMedium)
            MoodCalendar(days = dayMap)
        }
    }
}

@Composable
private fun YearHeatmapCard(points: List<MoodTrendPoint>) {
    if (points.isEmpty()) return
    val limited = points.takeLast(365)
    val dayMap: Map<Int, MoodLevel> = limited.associate { pt ->
        val dayOfYear = pt.date.toLocalDateTime(TimeZone.currentSystemDefault()).date.dayOfYear
        dayOfYear to pt.averageScore.toMoodLevel()
    }
    Card {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .semantics {
                    contentDescription = "Heatmap of last 365 days, ${limited.size} points"
                },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Heatmap (365 days)", style = MaterialTheme.typography.titleMedium)
            MoodCalendar(days = dayMap, daysInPeriod = 365)
        }
    }
}

@Composable
private fun RangeChips(selected: TrendRange, onSelect: (TrendRange) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TrendRange.values().forEach { range ->
            Surface(
                shape = CircleShape,
                color = if (range == selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                tonalElevation = if (range == selected) 4.dp else 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(range) }
                    .semantics {
                        contentDescription = "Select ${range.label} range ${if (range == selected) "selected" else "not selected"}"
                    }
            ) {
                Text(
                    text = range.label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun YearOverviewCard(points: List<MoodTrendPoint>) {
    if (points.isEmpty()) return
    val months = points.groupBy { it.date.toLocalDateTime(TimeZone.currentSystemDefault()).date.month }
    Card {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Year overview", style = MaterialTheme.typography.titleMedium)
            months.entries.sortedBy { it.key.value }.forEach { (month, items) ->
                val avg = items.map { it.averageScore }.average()
                Text(
                    "${month.name.lowercase().replaceFirstChar { it.titlecase() }}: ${String.format("%.2f", avg)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private enum class TrendRange(val label: String) {
    WEEK("7d"),
    MONTH("30d"),
    QUARTER("90d"),
    ALL("All")
}

private fun Double.toMoodLevel(): MoodLevel = when {
    this >= 4.5 -> MoodLevel.EXCELLENT
    this >= 3.5 -> MoodLevel.GOOD
    this >= 2.5 -> MoodLevel.NEUTRAL
    this >= 1.5 -> MoodLevel.POOR
    else -> MoodLevel.TERRIBLE
}
