package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
fun DonutChart(
    percentages: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    LaunchedEffect(percentages) {
        startAnimation = false
        delay(100)
        startAnimation = true
    }

    val animatedSweepAngles = percentages.map { percentage ->
        animateFloatAsState(
            targetValue = if (startAnimation) percentage * 360f else 0f,
            animationSpec = tween(durationMillis = 1000),
            label = "sweepAngle"
        ).value
    }

    val trackColor = AppTheme.colors.surface

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.width * 0.15f
            val radius = (size.width - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)
            val chartSize = Size(radius * 2, radius * 2)

            // Draw track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Draw slices
            var startAngle = -90f
            percentages.indices.forEach { index ->
                val sweepAngle = animatedSweepAngles[index]
                if (sweepAngle > 0f) {
                    drawArc(
                        color = colors.getOrElse(index) { colors.last() },
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = chartSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }
        }
    }
}
