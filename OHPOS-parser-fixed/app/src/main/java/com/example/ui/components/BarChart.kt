package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.delay

data class BarChartData(
    val label: String,
    val sales: Double,
    val orders: Int
)

@Composable
fun BarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxSales = data.maxOfOrNull { it.sales }?.toFloat()?.coerceAtLeast(1f) ?: 1f
    
    // Animation for bars
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(data) {
        startAnimation = false
        delay(50)
        startAnimation = true
    }

    val barColor = AppTheme.colors.accent
    val gridColor = AppTheme.colors.borderLight

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp, bottom = 40.dp, start = 8.dp, end = 8.dp)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val barWidth = size.width / data.size
                        val index = (offset.x / barWidth).toInt()
                        if (index in data.indices) {
                            selectedIndex = if (selectedIndex == index) null else index
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val barWidth = width / data.size
            val barPadding = barWidth * 0.3f
            
            // Draw horizontal grid lines
            val steps = 4
            for (i in 0..steps) {
                val y = height - (i * height / steps)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw bars
            data.forEachIndexed { index, item ->
                val targetHeight = (item.sales / maxSales).toFloat() * height
                val animatedHeight = if (startAnimation) targetHeight else 0f
                
                val x = (index * barWidth) + barPadding / 2
                val y = height - animatedHeight
                
                drawRoundRect(
                    color = if (selectedIndex == index) barColor else barColor.copy(alpha = 0.8f),
                    topLeft = Offset(x, y),
                    size = Size(barWidth - barPadding, animatedHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }
        
        // Tooltip
        selectedIndex?.let { index ->
            val item = data[index]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF222222))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.label,
                        style = AppTheme.typography.labelMedium,
                        color = AppTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.formatNoDecimals(item.sales),
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.textPrimary
                    )
                    Text(
                        text = "${item.orders} Orders",
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textSecondary
                    )
                }
            }
        }
        
        // X-Axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { item ->
                Text(
                    text = item.label,
                    style = AppTheme.typography.labelMedium,
                    color = AppTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
