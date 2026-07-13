package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.ui.theme.AppTheme

@Composable
fun PremiumBottomSheet(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(animationSpec = PremiumMotion.defaultSpring(), expandFrom = Alignment.Top),
        exit = shrinkVertically(animationSpec = PremiumMotion.defaultSpring(), shrinkTowards = Alignment.Top),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppTheme.radius.lg)
                .background(AppTheme.colors.surface)
                .border(AppTheme.dimensions.borderThickness, AppTheme.colors.borderLight, AppTheme.radius.lg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = PremiumMotion.defaultSpring())
            ) {
                content()
            }
        }
    }
}
