package com.example.ui.components

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.BoxScope

@Composable
fun PremiumModalOverlay(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    overlayContent: @Composable BoxScope.() -> Unit
) {
    val blurRadius by animateDpAsState(
        targetValue = if (isVisible) PremiumMotion.BLUR_RADIUS.dp else 0.dp,
        animationSpec = PremiumMotion.defaultSpring(),
        label = "blur"
    )

    BackHandler(enabled = isVisible) {
        onDismissRequest()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.blur(blurRadius)
                    } else Modifier
                )
        ) {
            content()
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()),
            exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = PremiumMotion.DIM_OPACITY))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onDismissRequest()
                    }
            )
        }

        overlayContent()
    }
}
