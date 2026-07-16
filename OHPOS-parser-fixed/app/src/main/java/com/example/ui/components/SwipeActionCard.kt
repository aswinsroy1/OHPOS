package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeActionCard(
    modifier: Modifier = Modifier,
    cardId: Any,
    currentlyOpenCardId: Any?,
    onCardOpen: (Any) -> Unit,
    onDelete: (() -> Unit)? = null,
    onPrint: (() -> Unit)? = null,
    isSwipeEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    
    val maxOffset = with(density) { 80.dp.toPx() }
    val threshold = maxOffset * 0.4f
    
    val offsetX = remember { Animatable(0f) }
    var hasCrossedDeleteThreshold by remember { mutableStateOf(false) }
    var hasCrossedPrintThreshold by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(true) }

    val globalOpenCardId = SwipeCardManager.currentlyOpenCardId
    val interactionTick = SwipeCardManager.interactionTick
    
    // 1. Immediate Single-Card Enforcement
    LaunchedEffect(globalOpenCardId) {
        if (globalOpenCardId != cardId && offsetX.value != 0f) {
            offsetX.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
        }
    }
    
    // 4. Auto Close Timer (Exactly 4 seconds of inactivity)
    LaunchedEffect(globalOpenCardId, interactionTick) {
        if (globalOpenCardId == cardId && abs(offsetX.targetValue) >= threshold) {
            delay(4000)
            if (SwipeCardManager.currentlyOpenCardId == cardId) {
                offsetX.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
                SwipeCardManager.currentlyOpenCardId = null
            }
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = shrinkVertically() + fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp) 
        ) {
            // Action Backgrounds
            if (isSwipeEnabled) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(vertical = 0.dp)
                        .clip(AppTheme.radius.lg)
                        .background(AppTheme.colors.accent.copy(alpha = 0.2f))
                ) {
                    // Delete Background (Left to Right drag)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .bouncyClickable {
                            SwipeCardManager.resetInteraction()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            coroutineScope.launch {
                                isVisible = false
                                delay(300)
                                onDelete?.invoke()
                            }
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(AppTheme.dimensions.listItemIconSize)
                            .padding(start = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = AppTheme.colors.accent,
                            modifier = Modifier.size(AppTheme.dimensions.iconSizeMd)
                        )
                    }
                }
                
                // Print Background (Right to Left drag)
                if (onPrint != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight()
                            .align(Alignment.CenterEnd)
                            .bouncyClickable {
                                SwipeCardManager.resetInteraction()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                coroutineScope.launch {
                                    offsetX.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
                                    if (SwipeCardManager.currentlyOpenCardId == cardId) {
                                        SwipeCardManager.currentlyOpenCardId = null
                                    }
                                    onPrint()
                                }
                            },
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(AppTheme.dimensions.listItemIconSize)
                                .padding(end = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Print,
                                contentDescription = "Print",
                                tint = AppTheme.colors.accent,
                                modifier = Modifier.size(AppTheme.dimensions.iconSizeMd)
                            )
                        }
                    }
                }
            }
            }

            // Foreground Content
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .pointerInput(isSwipeEnabled) {
                        if (!isSwipeEnabled) return@pointerInput
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                SwipeCardManager.resetInteraction()
                                coroutineScope.launch {
                                    if (offsetX.value > threshold) {
                                        offsetX.animateTo(maxOffset, spring(dampingRatio = 0.6f, stiffness = 400f))
                                        onCardOpen(cardId)
                                        SwipeCardManager.currentlyOpenCardId = cardId
                                        SwipeCardManager.openDirection = 1
                                    } else if (onPrint != null && offsetX.value < -threshold) {
                                        offsetX.animateTo(-maxOffset, spring(dampingRatio = 0.6f, stiffness = 400f))
                                        onCardOpen(cardId)
                                        SwipeCardManager.currentlyOpenCardId = cardId
                                        SwipeCardManager.openDirection = -1
                                    } else {
                                        offsetX.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
                                        if (SwipeCardManager.currentlyOpenCardId == cardId) {
                                            SwipeCardManager.currentlyOpenCardId = null
                                            SwipeCardManager.openDirection = 0
                                        }
                                    }
                                    hasCrossedDeleteThreshold = false
                                    hasCrossedPrintThreshold = false
                                }
                            },
                            onDragCancel = {
                                SwipeCardManager.resetInteraction()
                                coroutineScope.launch {
                                    offsetX.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
                                    if (SwipeCardManager.currentlyOpenCardId == cardId) {
                                        SwipeCardManager.currentlyOpenCardId = null
                                        SwipeCardManager.openDirection = 0
                                    }
                                }
                                hasCrossedDeleteThreshold = false
                                hasCrossedPrintThreshold = false
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            
                            // Immediately close other cards when dragging starts
                            if (SwipeCardManager.currentlyOpenCardId != cardId) {
                                SwipeCardManager.currentlyOpenCardId = cardId
                            }
                            SwipeCardManager.resetInteraction()

                            coroutineScope.launch {
                                val minOff = if (onPrint != null) -maxOffset * 1.2f else 0f
                                val newOffset = (offsetX.value + dragAmount).coerceIn(minOff, maxOffset * 1.2f)
                                offsetX.snapTo(newOffset)
                                
                                if (newOffset > threshold && !hasCrossedDeleteThreshold) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    hasCrossedDeleteThreshold = true
                                } else if (newOffset <= threshold) {
                                    hasCrossedDeleteThreshold = false
                                }
                                
                                if (onPrint != null) {
                                    if (newOffset < -threshold && !hasCrossedPrintThreshold) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        hasCrossedPrintThreshold = true
                                    } else if (newOffset >= -threshold) {
                                        hasCrossedPrintThreshold = false
                                    }
                                }
                            }
                        }
                    }
            ) {
                content()
                
                // 3. Tap to Close (Reliable overlay when card is open)
                if (globalOpenCardId == cardId && abs(offsetX.value) > 1f) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        SwipeCardManager.resetInteraction()
                                        coroutineScope.launch {
                                            offsetX.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
                                            if (SwipeCardManager.currentlyOpenCardId == cardId) {
                                                SwipeCardManager.currentlyOpenCardId = null
                                                SwipeCardManager.openDirection = 0
                                            }
                                        }
                                    }
                                )
                            }
                    )
                }
            }
        }
    }
}


// 5. Single Source of Truth
object SwipeCardManager {
    var currentlyOpenCardId by mutableStateOf<Any?>(null)
    var openDirection by mutableStateOf(0) // -1 for Left(Print), 1 for Right(Delete), 0 for None
    var interactionTick by mutableStateOf(0)
    
    fun resetInteraction() {
        interactionTick++
    }
}
