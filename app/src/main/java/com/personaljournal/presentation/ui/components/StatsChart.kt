package com.personaljournal.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.personaljournal.domain.model.MoodTrendPoint
import com.personaljournal.presentation.ui.theme.LocalMotionScale
import kotlin.math.roundToInt

@Composable
fun StatsChart(
    points: List<MoodTrendPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return
    val chartColor = MaterialTheme.colorScheme.primary
    val motionScale = LocalMotionScale.current
    val duration = (650 * motionScale).roundToInt().coerceAtLeast(0)
    val animationSpec =
        if (duration == 0) snap()
        else tween<Float>(durationMillis = duration, easing = { it })
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = animationSpec,
        label = "trendReveal"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val maxScore = points.maxOf { it.averageScore }.coerceAtLeast(1.0)
        val minScore = points.minOf { it.averageScore }
        val path = Path()
        val offsets = mutableListOf<Offset>()
        points.forEachIndexed { index, point ->
            val x = size.width * index / (points.size - 1).coerceAtLeast(1)
            val normalized = (point.averageScore - minScore) / (maxScore - minScore + 0.01)
            val y = size.height - (size.height * normalized).toFloat()
            offsets += Offset(x, y)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        val clipWidth = size.width * progress
        drawContext.canvas.save()
        drawContext.canvas.clipRect(0f, 0f, clipWidth, size.height)

        val fillPath = Path().apply {
            addPath(path)
            val lastPoint = offsets.last()
            lineTo(lastPoint.x, size.height)
            lineTo(offsets.first().x, size.height)
            close()
            fillType = PathFillType.EvenOdd
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    chartColor.copy(alpha = 0.35f),
                    chartColor.copy(alpha = 0.05f)
                )
            )
        )
        drawPath(
            path = path,
            color = chartColor,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )
        offsets.forEach { point ->
            drawCircle(
                color = chartColor,
                radius = 6f,
                center = point
            )
        }

        drawContext.canvas.restore()
    }
}
