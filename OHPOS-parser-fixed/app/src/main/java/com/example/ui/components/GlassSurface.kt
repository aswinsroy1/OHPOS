package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = AppTheme.radius.md,
    content: @Composable () -> Unit
) {
    // A subtle glass effect with border and semi-transparent background.
    Box(
        modifier = modifier
            .clip(shape)
            .background(AppTheme.colors.background.copy(alpha = 0.85f))
            .border(
                width = 1.dp,
                color = AppTheme.colors.divider,
                shape = shape
            )
    ) {
        content()
    }
}
