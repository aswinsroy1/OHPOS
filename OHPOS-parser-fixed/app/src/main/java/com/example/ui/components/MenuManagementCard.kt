package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme
import com.example.data.MenuItem
import com.example.util.CurrencyFormatter

@Composable
fun MenuManagementCard(
    menuItem: MenuItem,
    onLongPress: () -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    val cardAlpha = if (menuItem.isActive) 1f else 0.55f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = AppTheme.elevations.md,
                shape = AppTheme.radius.lg,
                spotColor = Color.Black,
                ambientColor = Color.Black
            )
            .clip(AppTheme.radius.lg)
            .background(AppTheme.colors.surface)
            .border(
                width = AppTheme.dimensions.borderThickness,
                color = Color.White.copy(alpha = 0.06f),
                shape = AppTheme.radius.lg
            )
            .pointerInput(menuItem, onLongPress, onClick) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    }
                )
            }
            .alpha(cardAlpha)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Food Image Placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(AppTheme.radius.md)
                    .background(AppTheme.colors.surfaceLighter)
                    .border(AppTheme.dimensions.borderThickness, Color.White.copy(alpha = 0.06f), AppTheme.radius.md),
                contentAlignment = Alignment.Center
            ) {
                if (menuItem.imageUrl.isNotBlank()) {
                    coil.compose.SubcomposeAsyncImage(
                        model = menuItem.imageUrl,
                        contentDescription = menuItem.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Restaurant,
                                    contentDescription = null,
                                    tint = AppTheme.colors.textSecondary,
                                    modifier = Modifier.size(AppTheme.dimensions.iconSizeMd)
                                )
                            }
                        },
                        error = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Restaurant,
                                    contentDescription = null,
                                    tint = AppTheme.colors.textSecondary,
                                    modifier = Modifier.size(AppTheme.dimensions.iconSizeMd)
                                )
                            }
                        }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Restaurant,
                        contentDescription = null,
                        tint = AppTheme.colors.textSecondary,
                        modifier = Modifier.size(AppTheme.dimensions.iconSizeMd)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(AppTheme.spacing.md))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = menuItem.name,
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.textPrimary.copy(alpha = 0.9f)
                )
                if (menuItem.category.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = menuItem.category,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textSecondary.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = CurrencyFormatter.format(menuItem.price),
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary
                )
            }
            
            if (!menuItem.isActive) {
                Box(
                    modifier = Modifier
                        .clip(AppTheme.radius.sm)
                        .background(AppTheme.colors.surfaceLighter)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Disabled",
                        style = AppTheme.typography.labelMedium,
                        color = AppTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}
