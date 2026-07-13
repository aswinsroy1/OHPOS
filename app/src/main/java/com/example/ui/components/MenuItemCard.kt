package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme
import com.example.data.MenuItem

import androidx.compose.material.icons.rounded.Remove

@Composable
fun MenuItemCard(
    menuItem: MenuItem,
    quantity: Int = 0,
    selectedOrderType: Int = 0,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isSelected = quantity > 0
    val effectivePrice = if (selectedOrderType == 1 && menuItem.deliveryPrice != null) menuItem.deliveryPrice else menuItem.price
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
                width = if (isSelected) 2.dp else AppTheme.dimensions.borderThickness,
                color = if (isSelected) AppTheme.colors.accent else Color.White.copy(alpha = 0.06f),
                shape = AppTheme.radius.lg
            )
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
                if (menuItem.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = menuItem.description,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textSecondary.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(AppTheme.spacing.md))
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(64.dp)
            ) {
                Text(
                    text = com.example.util.CurrencyFormatter.format(effectivePrice),
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary
                )
                
                if (isSelected) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(AppTheme.radius.sm)
                                .background(AppTheme.colors.surfaceLighter)
                                .border(AppTheme.dimensions.borderThickness, Color.White.copy(alpha = 0.1f), AppTheme.radius.sm)
                                .bouncyClickable { onRemoveClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Remove,
                                contentDescription = "Decrease",
                                tint = AppTheme.colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = quantity.toString(),
                            style = AppTheme.typography.titleMedium,
                            color = AppTheme.colors.textPrimary
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(AppTheme.radius.sm)
                                .background(AppTheme.colors.surfaceLighter)
                                .border(AppTheme.dimensions.borderThickness, Color.White.copy(alpha = 0.1f), AppTheme.radius.sm)
                                .bouncyClickable { onAddClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Increase",
                                tint = AppTheme.colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(AppTheme.radius.sm)
                            .background(AppTheme.colors.surfaceLighter)
                            .border(AppTheme.dimensions.borderThickness, Color.White.copy(alpha = 0.1f), AppTheme.radius.sm)
                            .bouncyClickable { onAddClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add",
                            tint = AppTheme.colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
