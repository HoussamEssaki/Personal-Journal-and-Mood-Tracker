package com.personaljournal.presentation.screens.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personaljournal.domain.model.CorrelationInsight
import com.personaljournal.domain.model.StatsSnapshot
import com.personaljournal.domain.model.TrendDirection
import com.personaljournal.presentation.viewmodel.TrendWindow
import com.personaljournal.presentation.viewmodel.TrendsUiState
import com.personaljournal.presentation.viewmodel.TrendsViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TrendsInsightsRoute(
    viewModel: TrendsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TrendsInsightsScreen(
        state = state,
        onSelectWindow = viewModel::selectWindow
    )
}

@Composable
fun TrendsInsightsScreen(
    state: TrendsUiState,
    onSelectWindow: (TrendWindow) -> Unit
) {
    val background = Color(0xFF050F1F)
    val positiveFactors = state.snapshot.correlations.filter { it.correlation >= 0 }
    val negativeFactors = state.snapshot.correlations.filter { it.correlation < 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Trends & Insights",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TrendWindow.values().forEach { window ->
                OutlinedButton(
                    onClick = { onSelectWindow(window) },
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (state.selectedWindow == window) Color(0xFF2E80FF) else Color(0xFF243141)
                    )
                ) {
                    Text(
                        window.label,
                        color = if (state.selectedWindow == window) Color.White else Color(0xFF75859D)
                    )
                }
            }
        }
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2E80FF))
            }
        } else {
            InsightSummaryCard(snapshot = state.snapshot, window = state.selectedWindow)
            FactorCard(
                title = "Factors Linked to Positive Moods",
                tint = Color(0xFF36D288),
                data = positiveFactors
            )
            FactorCard(
                title = "Factors Linked to Negative Moods",
                tint = Color(0xFFFF5F5F),
                data = negativeFactors
            )
            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun InsightSummaryCard(
    snapshot: StatsSnapshot,
    window: TrendWindow
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C1828))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Mood Over Time", color = Color(0xFF9AB3D7))
            Text(
                text = summaryLabel(snapshot.averageMoodLevel),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Text(
                text = "${window.label} • Entries ${snapshot.totalEntries}",
                color = Color(0xFF5DE28D)
            )
            TrendsLineChart(
                values = snapshot.recentTrend.map { (it.averageScore / 5.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun TrendsLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    val chartValues = if (values.isEmpty()) {
        listOf(0.4f, 0.45f, 0.5f, 0.55f, 0.52f, 0.6f)
    } else {
        values
    }
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val path = Path()
        chartValues.forEachIndexed { index, value ->
            val x = if (chartValues.size == 1) size.width / 2 else
                (index / (chartValues.lastIndex.toFloat())).coerceIn(0f, 1f) * size.width
            val y = (1 - value.coerceIn(0f, 1f)) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = Color(0xFF4DA1FF),
            style = Stroke(width = 6f)
        )
    }
}

@Composable
private fun FactorCard(
    title: String,
    tint: Color,
    data: List<CorrelationInsight>
) {
    if (data.isEmpty()) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C1828))
                .padding(20.dp)
        ) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            data.take(5).forEach { insight ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(insight.factor, color = Color.White)
                    Text(
                        text = formatCorrelation(insight),
                        color = tint,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun summaryLabel(averageMood: Double): String = when {
    averageMood >= 4.2 -> "Excellent Mood"
    averageMood >= 3.5 -> "Mostly Positive"
    averageMood >= 2.8 -> "Holding Steady"
    else -> "Needs Attention"
}

private fun formatCorrelation(insight: CorrelationInsight): String {
    val sign = if (insight.correlation >= 0) "+" else "-"
    val percent = (abs(insight.correlation) * 100).roundToInt()
    val trendIcon = when (insight.trend) {
        TrendDirection.UP -> "↑"
        TrendDirection.DOWN -> "↓"
        TrendDirection.STABLE -> "→"
    }
    return "$sign$percent% $trendIcon"
}
