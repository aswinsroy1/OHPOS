package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme

@Composable
fun AppListItem(
    title: String,
    subtitle: String,
    trailingText: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    statusText: String? = null,
    statusColor: Color? = null,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    isPendingDeletion: Boolean = false
) {
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
            .border(AppTheme.dimensions.borderThickness, Color.White.copy(alpha = 0.06f), AppTheme.radius.lg)
            .liftClickable(
                onLongClick = { onLongPress?.invoke() },
                onClick = { onClick?.invoke() }
            )
            .padding(16.dp)
            .alpha(if (isPendingDeletion) 0.65f else 1f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(AppTheme.dimensions.listItemIconSize)
                    .clip(AppTheme.radius.md)
                    .background(AppTheme.colors.surfaceLighter)
                    .border(AppTheme.dimensions.borderThickness, Color.White.copy(alpha = 0.06f), AppTheme.radius.md),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppTheme.colors.textSecondary,
                    modifier = Modifier.size(AppTheme.dimensions.iconSizeMd)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colors.textPrimary.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isPendingDeletion) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Pending Deletion",
                            tint = AppTheme.colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = subtitle,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textSecondary.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    if (isPendingDeletion) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(AppTheme.radius.sm)
                                .background(AppTheme.colors.textSecondary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Pending Deletion",
                                style = AppTheme.typography.labelMedium,
                                color = AppTheme.colors.textSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    if (statusText != null && statusColor != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(AppTheme.radius.sm)
                                .background(statusColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusText,
                                style = AppTheme.typography.labelMedium,
                                color = statusColor,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
            
            Text(
                text = trailingText,
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary
            )
        }
    }
}
