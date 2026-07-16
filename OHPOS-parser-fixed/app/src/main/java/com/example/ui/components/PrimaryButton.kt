package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = AppTheme.colors.accent,
    contentColor: Color = Color.Unspecified,
    enabled: Boolean = true
) {
    // Special-case text color for Monochrome accent to guarantee legibility.
    val isMonochrome = containerColor == AppTheme.colors.textPrimary
    val isDarkMode = AppTheme.colors.background == Color(0xFF1B1B1D)
    
    val resolvedContentColor = if (contentColor != Color.Unspecified) {
        contentColor
    } else if (isMonochrome) {
        AppTheme.colors.background
    } else {
        if (isDarkMode) AppTheme.colors.background else Color.White
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(AppTheme.radius.md)
            .background(if (enabled) containerColor else containerColor.copy(alpha = 0.5f))
            .then(if (enabled) Modifier.bouncyClickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTheme.typography.titleMedium,
            color = resolvedContentColor
        )
    }
}
