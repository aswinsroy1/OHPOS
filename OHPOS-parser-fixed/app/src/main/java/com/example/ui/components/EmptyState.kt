package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.AutoMirrored.Rounded.ReceiptLong
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.surface)
                .border(AppTheme.dimensions.borderThickness, AppTheme.colors.borderLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppTheme.colors.accent,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
        
        Text(
            text = title,
            style = AppTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary
        )
        
        Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
        
        Text(
            text = subtitle,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 48.dp)
        )
    }
}
