package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.theme.AppTheme

@Composable
fun AppScaffold(
    bottomBar: @Composable () -> Unit = {},
    topBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
    ) {
        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }

        // Top Bar
        Box(
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            topBar()
        }

        // Bottom Bar
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            bottomBar()
        }
    }
}
