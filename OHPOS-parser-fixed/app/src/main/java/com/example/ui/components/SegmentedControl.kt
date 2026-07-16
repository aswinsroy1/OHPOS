package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme

@Composable
fun SegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AppTheme.dimensions.buttonHeight)
            .clip(AppTheme.radius.md)
            .background(AppTheme.colors.surface)
            .border(
                AppTheme.dimensions.borderThickness,
                AppTheme.colors.borderLight,
                AppTheme.radius.md
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) AppTheme.colors.accent else Color.Transparent,
                label = "background"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) AppTheme.colors.iconDark else AppTheme.colors.textSecondary,
                label = "textColor"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(AppTheme.radius.md)
                    .background(backgroundColor)
                    .bouncyClickable { onItemSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    style = AppTheme.typography.labelMedium,
                    color = textColor
                )
            }
        }
    }
}
