package com.example.ui.components

import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.dp

object PremiumMotion {
    const val BLUR_RADIUS = 8f
    const val DIM_OPACITY = 0.2f

    fun <T> defaultSpring() = spring<T>(
        dampingRatio = 0.76f,
        stiffness = 180f
    )
}
