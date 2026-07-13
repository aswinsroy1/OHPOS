package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView

enum class NavItem(val title: String, val icon: ImageVector) {
    Home("Home", Icons.Rounded.Home),
    Billing("Billing", Icons.AutoMirrored.Rounded.ReceiptLong),
    Menu("Menu", Icons.Rounded.RestaurantMenu),
    Reports("Reports", Icons.Rounded.BarChart)
}

@Composable
fun CustomBottomNavigation(
    selectedItem: NavItem,
    onItemSelected: (NavItem) -> Unit,
    onReportsLongClick: (() -> Unit)? = null,
    showReportsDot: Boolean = false,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.background)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
        ) {
            val totalWidth = maxWidth
            val rowPadding = 32.dp
            val availableWidth = totalWidth - (rowPadding * 2)
            val itemsCount = NavItem.values().size
            val itemSize = 64.dp
            val gapCount = itemsCount - 1
            val gapSize = if (gapCount > 0) (availableWidth - (itemSize * itemsCount)) / gapCount else 0.dp
            
            val selectedIndex = NavItem.values().indexOf(selectedItem)
            
            // Indicator animation: continuous movement, no bounce, constant smooth velocity
            val indicatorOffset by animateDpAsState(
                targetValue = rowPadding + (itemSize * selectedIndex) + (gapSize * selectedIndex) + ((itemSize - 42.dp) / 2),
                animationSpec = spring(
                    dampingRatio = 1f,
                    stiffness = 250f
                ),
                label = "indicatorOffset"
            )

            // Active indicator
            Box(
                modifier = Modifier
                    .graphicsLayer { translationX = indicatorOffset.toPx() }
                    .size(42.dp)
                    .align(Alignment.CenterStart)
                    .clip(CircleShape)
                    .background(AppTheme.colors.accent)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .padding(horizontal = rowPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem.values().forEach { item ->
                    val isSelected = selectedItem == item
                    
                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) AppTheme.colors.background else AppTheme.colors.textSecondary,
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                        label = "iconTint"
                    )

                    val iconScaleAnim = remember { Animatable(1f) }
                    
                    LaunchedEffect(isSelected) {
                        if (isSelected) {
                            iconScaleAnim.snapTo(1f)
                            iconScaleAnim.animateTo(
                                targetValue = 0.97f,
                                animationSpec = tween(durationMillis = 60, easing = LinearOutSlowInEasing)
                            )
                            iconScaleAnim.animateTo(
                                targetValue = 1.04f,
                                animationSpec = tween(durationMillis = 90, easing = FastOutLinearInEasing)
                            )
                            iconScaleAnim.animateTo(
                                targetValue = 1.0f,
                                animationSpec = tween(durationMillis = 70, easing = FastOutSlowInEasing)
                            )
                        } else {
                            iconScaleAnim.animateTo(
                                targetValue = 1.0f,
                                animationSpec = tween(durationMillis = 150)
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                        onItemSelected(item)
                                    },
                                    onLongPress = {
                                        if (item == NavItem.Reports) onReportsLongClick?.invoke()
                                    }
                                )
                            }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = iconTint,
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        scaleX = iconScaleAnim.value
                                        scaleY = iconScaleAnim.value
                                    }
                            )
                            if (item == NavItem.Reports && showReportsDot) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 4.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AppTheme.colors.accent)
                                        .graphicsLayer { 
                                            scaleX = iconScaleAnim.value
                                            scaleY = iconScaleAnim.value 
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
