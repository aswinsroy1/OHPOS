package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        contentPadding = PaddingValues(horizontal = AppTheme.spacing.lg)
    ) {
        items(categories, key = { it }) { category ->
            val isSelected = category == selectedCategory
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) AppTheme.colors.accent else AppTheme.colors.surface,
                label = "background"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) AppTheme.colors.iconDark else AppTheme.colors.textSecondary,
                label = "textColor"
            )

            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(AppTheme.radius.md)
                    .background(backgroundColor)
                    .border(
                        AppTheme.dimensions.borderThickness,
                        if (isSelected) Color.Transparent else AppTheme.colors.borderLight,
                        AppTheme.radius.md
                    )
                    .bouncyClickable { onCategorySelected(category) }
                    .padding(horizontal = AppTheme.spacing.md),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category,
                    style = AppTheme.typography.labelMedium,
                    color = textColor
                )
            }
        }
    }
}
