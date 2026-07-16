package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.components.PremiumMotion
import com.example.ui.theme.AppTheme

enum class SettingsDestination {
    Main,
    Printers,
    ReceiptLayout,
    PaperSize,
    AutoBackup
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun SettingsNavGraph(
    onBackClick: () -> Unit,
    initialDestination: SettingsDestination = SettingsDestination.Main
) {
    var currentDestination by remember { mutableStateOf(initialDestination) }

    LaunchedEffect(initialDestination) {
        currentDestination = initialDestination
    }

    androidx.activity.compose.BackHandler(enabled = currentDestination != SettingsDestination.Main) {
        currentDestination = SettingsDestination.Main
    }
    
    Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)) {
        // Main Settings
        AnimatedVisibility(
            visible = currentDestination == SettingsDestination.Main,
            enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = PremiumMotion.defaultSpring()
            ),
            exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = PremiumMotion.defaultSpring()
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            SettingsScreen(
                onBackClick = onBackClick,
                onNavigateToPrinters = { currentDestination = SettingsDestination.Printers },
                onNavigateToReceiptLayout = { currentDestination = SettingsDestination.ReceiptLayout },
                onNavigateToAutoBackup = { currentDestination = SettingsDestination.AutoBackup }
            )
        }
        
        // Printer Settings
        AnimatedVisibility(
            visible = currentDestination == SettingsDestination.Printers,
            enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = PremiumMotion.defaultSpring()
            ),
            exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = PremiumMotion.defaultSpring()
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            PrinterSettingsScreen(
                onBackClick = { currentDestination = SettingsDestination.Main }
            )
        }
        
        // Receipt Layout Settings
        AnimatedVisibility(
            visible = currentDestination == SettingsDestination.ReceiptLayout,
            enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = PremiumMotion.defaultSpring()
            ),
            exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = PremiumMotion.defaultSpring()
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            ReceiptLayoutScreen(
                onBackClick = { currentDestination = SettingsDestination.Main }
            )
        }
        
        // Auto Backup Settings
        AnimatedVisibility(
            visible = currentDestination == SettingsDestination.AutoBackup,
            enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = PremiumMotion.defaultSpring()
            ),
            exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = PremiumMotion.defaultSpring()
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            AutoBackupSettingsScreen(
                onBackClick = { currentDestination = SettingsDestination.Main }
            )
        }
    }
}
