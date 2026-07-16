package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppTheme.spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTheme.typography.titleMedium,
            color = AppTheme.colors.textPrimary
        )

        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = AppTheme.typography.labelMedium,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier.bouncyClickable(onClick = onActionClick)
            )
        }
    }
}
