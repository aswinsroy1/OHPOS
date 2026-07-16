package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme

@Composable
fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search items...",
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Rounded.QrCodeScanner
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
            )
            .padding(horizontal = AppTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = "Search",
            tint = AppTheme.colors.textSecondary,
            modifier = Modifier.size(AppTheme.dimensions.iconSizeMd)
        )
        
        Spacer(modifier = Modifier.width(AppTheme.spacing.md))
        
        Box(
            modifier = Modifier.weight(1f)
        ) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.textPrimary),
                cursorBrush = SolidColor(AppTheme.colors.accent),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.width(AppTheme.spacing.md))
        
        Icon(
            imageVector = trailingIcon,
            contentDescription = "Scan Barcode",
            tint = AppTheme.colors.textSecondary,
            modifier = Modifier.size(AppTheme.dimensions.iconSizeMd)
        )
    }
}
