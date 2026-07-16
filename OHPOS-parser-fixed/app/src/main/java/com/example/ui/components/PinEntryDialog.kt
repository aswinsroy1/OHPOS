package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PinEntryDialog(
    isVisible: Boolean,
    title: String = "Enter PIN",
    subtitle: String = "Enter 4-digit PIN",
    isSetupMode: Boolean = false,
    externalError: Boolean = false,
    onPinEntered: (String) -> Unit,
    onCancel: () -> Unit
) {
    if (!isVisible) return

    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    fun shake() {
        scope.launch {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showError = true
            shakeOffset.animateTo(15f, animationSpec = tween(50, easing = LinearEasing))
            shakeOffset.animateTo(-15f, animationSpec = tween(50, easing = LinearEasing))
            shakeOffset.animateTo(15f, animationSpec = tween(50, easing = LinearEasing))
            shakeOffset.animateTo(-15f, animationSpec = tween(50, easing = LinearEasing))
            shakeOffset.animateTo(0f, animationSpec = tween(50, easing = LinearEasing))
            delay(1500)
            showError = false
        }
    }

    LaunchedEffect(externalError) {
        if (externalError) {
            errorMessage = "Incorrect PIN"
            shake()
        }
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(AppTheme.spacing.lg),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(320.dp)
                    .offset(x = shakeOffset.value.dp)
                    .clip(AppTheme.radius.lg)
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.borderLight, AppTheme.radius.lg)
                    .padding(AppTheme.spacing.xl)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = AppTheme.colors.accent,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                    
                    val displayTitle = if (isSetupMode && isConfirming) "Confirm PIN" else title
                    val displaySubtitle = if (showError) errorMessage else if (isSetupMode && isConfirming) "Re-enter to confirm" else subtitle
                    
                    Text(
                        text = displayTitle,
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = displaySubtitle,
                        style = AppTheme.typography.bodyMedium,
                        color = if (showError) Color(0xFFE57373) else AppTheme.colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val currentPinLength = if (isConfirming) confirmPin.length else pin.length
                        for (i in 0 until 4) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i < currentPinLength) AppTheme.colors.accent
                                        else AppTheme.colors.surfaceLighter
                                    )
                                    .border(
                                        1.dp,
                                        if (i < currentPinLength) AppTheme.colors.accent else AppTheme.colors.borderLight,
                                        CircleShape
                                    )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val rows = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("Cancel", "0", "Del")
                        )
                        
                        rows.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                row.forEach { key ->
                                    val isAction = key == "Cancel" || key == "Del"
                                    Button(
                                        onClick = {
                                            if (key == "Cancel") {
                                                onCancel()
                                            } else if (key == "Del") {
                                                if (isConfirming) {
                                                    if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                                                } else {
                                                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                                }
                                            } else {
                                                if (isConfirming) {
                                                    if (confirmPin.length < 4) confirmPin += key
                                                    if (confirmPin.length == 4) {
                                                        if (confirmPin == pin) {
                                                            onPinEntered(pin)
                                                            pin = ""
                                                            confirmPin = ""
                                                            isConfirming = false
                                                        } else {
                                                            errorMessage = "PINs do not match"
                                                            shake()
                                                            confirmPin = ""
                                                        }
                                                    }
                                                } else {
                                                    if (pin.length < 4) pin += key
                                                    if (pin.length == 4) {
                                                        if (isSetupMode) {
                                                            isConfirming = true
                                                        } else {
                                                            onPinEntered(pin)
                                                            pin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isAction) AppTheme.colors.surfaceLighter else AppTheme.colors.surface,
                                            contentColor = AppTheme.colors.textPrimary
                                        ),
                                        elevation = null,
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = key,
                                            style = if (isAction) AppTheme.typography.labelMedium else AppTheme.typography.titleLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
