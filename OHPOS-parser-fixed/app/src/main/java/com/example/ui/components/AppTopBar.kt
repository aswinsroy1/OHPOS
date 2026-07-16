package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme

@Composable
fun AppTopBar(
    title: String? = null,
    subtitle: @Composable (() -> Unit)? = null,
    onMenuClick: () -> Unit = {},
    onBackClick: (() -> Unit)? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.Person,
    onTrailingClick: () -> Unit = {},
    trailingContentDescription: String = "Profile",
    showTrailingIcon: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppTheme.spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.surface)
                .bouncyClickable {
                    if (onBackClick != null) onBackClick() else onMenuClick()
                },
            contentAlignment = Alignment.Center
        ) {
            if (onBackClick != null) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = AppTheme.colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .size(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AppTheme.colors.textPrimary))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AppTheme.colors.textPrimary))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AppTheme.colors.textPrimary))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AppTheme.colors.textPrimary))
                    }
                }
            }
        }

        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (title != null) {
                androidx.compose.material3.Text(
                    text = title,
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary
                )
            }
            if (subtitle != null) {
                subtitle()
            }
        }

        // Trailing icon
        if (showTrailingIcon) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .border(2.dp, AppTheme.colors.divider, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.surface)
                    .bouncyClickable { onTrailingClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = trailingContentDescription,
                    tint = AppTheme.colors.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Box(modifier = Modifier.size(48.dp))
        }
    }
}
