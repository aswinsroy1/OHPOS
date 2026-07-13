package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    icon: ImageVector,
    label: String = "",
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.radius.lg)
            .background(AppTheme.colors.surface)
            .border(
                width = AppTheme.dimensions.borderThickness,
                color = Color.White.copy(alpha = 0.06f),
                shape = AppTheme.radius.lg
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppTheme.colors.textSecondary,
                modifier = Modifier.size(24.dp).padding(top = if (singleLine) 0.dp else 4.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = AppTheme.typography.labelMedium,
                    color = AppTheme.colors.textPrimary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = AppTheme.typography.bodyLarge.copy(color = AppTheme.colors.textPrimary),
                    singleLine = singleLine,
                    maxLines = maxLines,
                    keyboardOptions = keyboardOptions,
                    cursorBrush = SolidColor(AppTheme.colors.accent),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = AppTheme.typography.bodyLarge,
                                color = AppTheme.colors.textSecondary.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                )
            }
            
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(12.dp))
                trailingIcon()
            }
        }
    }
}
