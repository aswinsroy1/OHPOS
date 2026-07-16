package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun StatisticsCard(
    title: String,
    value: String,
    subtitle: String,
    backTitle: String = "",
    backValue: String = "",
    backSubtitle: String = "",
    icon: ImageVector,
    backgroundBrush: Brush = androidx.compose.ui.graphics.SolidColor(AppTheme.colors.surfaceLighter),
    textColor: Color = AppTheme.colors.textPrimary,
    badgeColor: Color = AppTheme.colors.accent,
    badgeIconColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    var flipped by remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    
    val hasBack = backTitle.isNotEmpty()
    
    // Use derivedStateOf to only recompose when this boolean changes, 
    // not on every single frame of the animation.
    val isBackVisible by remember {
        derivedStateOf { rotation.value > 90f }
    }

    val cardShape = AppTheme.radius.xl
    Box(
        modifier = modifier
            .aspectRatio(0.9f)
            .graphicsLayer {
                val fraction = if (hasBack) 1f - kotlin.math.abs(rotation.value - 90f) / 90f else 0f
                if (hasBack) {
                    rotationY = rotation.value
                    cameraDistance = 16f * density
                    
                    scaleX = 1f - (0.03f * fraction)
                    scaleY = 1f - (0.03f * fraction)
                }
                shadowElevation = 16.dp.toPx() + (8.dp.toPx() * fraction)
                shape = cardShape
                ambientShadowColor = Color(0x10000000)
                spotShadowColor = Color(0x10000000)
                clip = true
            }
            .background(backgroundBrush)
            .then(
                if (AppTheme.colors.background == Color(0xFF000000)) {
                    Modifier.border(1.dp, AppTheme.colors.borderLight, cardShape)
                } else {
                    Modifier
                }
            )
            .liftClickable { 
                if (hasBack) {
                    if (!rotation.isRunning) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        flipped = !flipped 
                        scope.launch {
                            rotation.animateTo(
                                targetValue = if (flipped) 180f else 0f,
                                animationSpec = tween(
                                    durationMillis = 600,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                    }
                } else {
                    onClick()
                }
            }
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (isBackVisible && hasBack) {
                        rotationY = 180f
                    }
                },
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = AppTheme.radius.md,
                        spotColor = Color(0x40000000),
                        ambientColor = Color(0x40000000)
                    )
                    .clip(AppTheme.radius.md)
                    .background(androidx.compose.ui.graphics.SolidColor(badgeColor))
                    .border(
                        width = AppTheme.dimensions.borderThickness,
                        color = badgeColor.copy(alpha = 0.2f),
                        shape = AppTheme.radius.md
                    ),
                contentAlignment = Alignment.Center
            ) {
                val resolvedIconColor = if (badgeIconColor != Color.Unspecified) badgeIconColor else {
                    val isMonochrome = badgeColor == AppTheme.colors.textPrimary
                    if (isMonochrome) AppTheme.colors.background else (if (badgeColor.luminance() > 0.5f) Color.Black else Color.White)
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = resolvedIconColor,
                    modifier = Modifier.size(AppTheme.dimensions.iconSizeSm)
                )
            }

            Text(
                text = if (isBackVisible && hasBack) backTitle else title,
                style = AppTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.6f)
            )
            
            Text(
                text = if (isBackVisible && hasBack) backValue else value,
                style = AppTheme.typography.numberLarge.copy(fontSize = 28.sp),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = if (isBackVisible && hasBack) backSubtitle else subtitle,
                style = AppTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.6f)
            )
        }
    }
}
