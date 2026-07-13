package com.example.ui.screens

import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import kotlinx.coroutines.launch


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NavItem
import com.example.ui.components.PremiumModalOverlay
import com.example.ui.components.CustomBottomNavigation
import com.example.ui.components.AppScaffold
import com.example.ui.components.PremiumMotion
import com.example.ui.components.bouncyClickable
import com.example.ui.theme.AppTheme
import com.example.util.PinManager
import com.example.ui.components.PinEntryDialog
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast


import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer



@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun MainScreenContent() {
    val dayClosingViewModel: DayClosingViewModel = viewModel()
    val isDayClosed by dayClosingViewModel.isDayClosed.collectAsState()
    val showReportsDot = !isDayClosed
    
    var showDayClosingMenu by remember { mutableStateOf(false) }
    var showDayClosingScreen by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableStateOf(NavItem.Home) }
    var showAppMenu by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showDeletionRequests by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var isPinError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val pinManager = remember { PinManager(context) }
    
    var backPressCount by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    
    
    BackHandler {
        if (showDayClosingMenu) {
            showDayClosingMenu = false
        } else if (showAppMenu) {
            showAppMenu = false
        } else if (showPinDialog) {
            showPinDialog = false
        } else if (showProfile) {
            showProfile = false
        } else if (showDayClosingScreen) {
            showDayClosingScreen = false
        } else if (showDeletionRequests) {
            showDeletionRequests = false
        } else if (showSettings) {
            showSettings = false
        } else if (selectedTab != NavItem.Home) {
            selectedTab = NavItem.Home
        } else {
            if (backPressCount == 0) {
                backPressCount++
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                scope.launch {
                    kotlinx.coroutines.delay(2000)
                    backPressCount = 0
                }
            } else {
                val activity = context as? android.app.Activity
                activity?.finish()
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        PremiumModalOverlay(
            isVisible = showAppMenu,
            onDismissRequest = { showAppMenu = false },
            content = {
                AppScaffold(
                    bottomBar = {
                        CustomBottomNavigation(
                            selectedItem = selectedTab,
                            onItemSelected = { 
                                selectedTab = it
                                showSettings = false
                            },
                            onReportsLongClick = { showDayClosingMenu = true },
                            showReportsDot = showReportsDot
                        )
                    }                ) {
                    val animatedSelectedTabIndex by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = selectedTab.ordinal.toFloat(),
                        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 1f, stiffness = 250f),
                        label = "tabIndex"
                    )

                    Box(modifier = Modifier.fillMaxSize()) {
                        NavItem.values().forEach { navItem ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        translationX = (navItem.ordinal - animatedSelectedTabIndex) * size.width
                                    }
                            ) {
                            when (navItem) {
                                NavItem.Home -> HomeScreen(
                                    selectedTab = selectedTab,
                                    onTabSelected = { 
                                        selectedTab = it
                                        showSettings = false
                                    },
                                    onMenuClick = { showAppMenu = true },
                                    onProfileClick = { showProfile = true },
                                    onReportsLongClick = { showDayClosingMenu = true },
                                    showReportsDot = showReportsDot
                                )
                                NavItem.Billing -> BillingScreen(
                                    selectedTab = selectedTab,
                                    onTabSelected = { 
                                        selectedTab = it
                                        showSettings = false
                                    },
                                    onMenuClick = { showAppMenu = true },
                                    onOpenSettings = { showSettings = true },
                                    onReportsLongClick = { showDayClosingMenu = true },
                                    showReportsDot = showReportsDot
                                )
                                NavItem.Menu -> MenuScreen(
                                    selectedTab = selectedTab,
                                    onTabSelected = { 
                                        selectedTab = it
                                        showSettings = false
                                    },
                                    onMenuClick = { showAppMenu = true },
                                    onReportsLongClick = { showDayClosingMenu = true },
                                    showReportsDot = showReportsDot
                                )
                                NavItem.Reports -> ReportsScreen(
                                    selectedTab = selectedTab,
                                    onTabSelected = { 
                                        selectedTab = it
                                        showSettings = false
                                    },
                                    onMenuClick = { showAppMenu = true },
                                    onReportsLongClick = { showDayClosingMenu = true },
                                    showReportsDot = showReportsDot,
                                    onDayClosingClick = { showDayClosingScreen = true }
                                )
                            }
                        }
                        } // end of forEach
                }
                }
            },
            overlayContent = {
                AnimatedVisibility(
                    visible = showAppMenu,
                    enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + slideInVertically(
                        initialOffsetY = { -it / 4 },
                        animationSpec = PremiumMotion.defaultSpring()
                    ),
                    exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + slideOutVertically(
                        targetOffsetY = { -it / 4 },
                        animationSpec = PremiumMotion.defaultSpring()
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(AppTheme.spacing.lg)
                            .windowInsetsPadding(WindowInsets.statusBars)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = 56.dp)
                                .width(220.dp)
                                .clip(AppTheme.radius.lg)
                                .background(AppTheme.colors.surface)
                                .border(
                                    1.dp,
                                    AppTheme.colors.borderLight,
                                    AppTheme.radius.lg
                                )
                                .padding(AppTheme.spacing.sm)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(AppTheme.radius.md)
                                        .bouncyClickable {
                                            showAppMenu = false
                                            showSettings = true
                                        }
                                        .padding(AppTheme.spacing.md),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Settings,
                                        contentDescription = "Settings",
                                        tint = AppTheme.colors.textPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                                    Text(
                                        text = "Settings",
                                        style = AppTheme.typography.bodyLarge,
                                        color = AppTheme.colors.textPrimary
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(AppTheme.radius.md)
                                        .bouncyClickable {
                                            showAppMenu = false
                                            showDeletionRequests = true
                                        }
                                        .padding(AppTheme.spacing.md),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "Deletion Requests",
                                        tint = AppTheme.colors.textPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                                    Text(
                                        text = "Deletion Requests",
                                        style = AppTheme.typography.bodyLarge,
                                        color = AppTheme.colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )
        


        AnimatedVisibility(
            visible = showDeletionRequests,
            enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = PremiumMotion.defaultSpring()
            ),
            exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = PremiumMotion.defaultSpring()
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            DeletionRequestsScreen(
                onBackClick = {
                    selectedTab = NavItem.Home
                    showDeletionRequests = false
                },
                selectedTab = selectedTab,
                onTabSelected = { 
                    selectedTab = it
                    showDeletionRequests = false
                }
            )
        }

        if (showDayClosingMenu) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showDayClosingMenu = false },
                containerColor = AppTheme.colors.background,
                dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle(color = AppTheme.colors.borderLight) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.spacing.lg)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Quick Actions",
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier.padding(bottom = AppTheme.spacing.md)
                    )
                    
                    val items = listOf(
                        "Close Business Day" to { 
                            showDayClosingMenu = false
                            showDayClosingScreen = true
                        },
                        "Today's Report" to {
                            showDayClosingMenu = false
                            selectedTab = NavItem.Reports
                        },
                        "Weekly Report" to {
                            showDayClosingMenu = false
                            selectedTab = NavItem.Reports
                        },
                        "Export Reports" to {
                            showDayClosingMenu = false
                            selectedTab = NavItem.Reports
                        },
                        "Cancel" to { showDayClosingMenu = false }
                    )
                    
                    items.forEach { (title, onClick) ->
                        Text(
                            text = title,
                            style = AppTheme.typography.bodyLarge,
                            color = if (title == "Cancel") AppTheme.colors.textSecondary else AppTheme.colors.textPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(AppTheme.radius.md)
                                .bouncyClickable(onClick = onClick)
                                .padding(AppTheme.spacing.md)
                        )
                    }
                }
            }
        }
        
        AnimatedVisibility(
            visible = showDayClosingScreen,
            enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = PremiumMotion.defaultSpring()
            ),
            exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = PremiumMotion.defaultSpring()
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            DayClosingScreen(
                onBackClick = { showDayClosingScreen = false }
            )
        }

        AnimatedVisibility(
            visible = showSettings,
            enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = PremiumMotion.defaultSpring()
            ),
            exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = PremiumMotion.defaultSpring()
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            SettingsNavGraph(
                onBackClick = { showSettings = false }
            )
        }

        AnimatedVisibility(
            visible = showProfile,
            enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = PremiumMotion.defaultSpring()
            ),
            exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = PremiumMotion.defaultSpring()
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            ProfileScreen(
                onBackClick = {
                    showProfile = false
                }
            )
        }
    }
}


@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun MainScreen() {
    var showSplash by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
    
    androidx.compose.animation.Crossfade(
        targetState = showSplash,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300),
        label = "SplashTransition"
    ) { isSplash ->
        if (isSplash) {
            AnimatedReceiptSplash(onSplashComplete = { showSplash = false })
        } else {
            MainScreenContent()
        }
    }
}

@Composable
fun AnimatedReceiptSplash(onSplashComplete: () -> Unit) {
    val outlineProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    val line1Progress = remember { androidx.compose.animation.core.Animatable(0f) }
    val line2Progress = remember { androidx.compose.animation.core.Animatable(0f) }
    val line3Progress = remember { androidx.compose.animation.core.Animatable(0f) }
    val line4Progress = remember { androidx.compose.animation.core.Animatable(0f) }
    val line5Progress = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        // Outline 0-350ms
        launch {
            outlineProgress.animateTo(
                1f,
                animationSpec = androidx.compose.animation.core.tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
        }
        
        // Lines with stagger
        launch {
            kotlinx.coroutines.delay(350)
            line1Progress.animateTo(1f, androidx.compose.animation.core.tween(150, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        }
        launch {
            kotlinx.coroutines.delay(420)
            line2Progress.animateTo(1f, androidx.compose.animation.core.tween(150, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        }
        launch {
            kotlinx.coroutines.delay(490)
            line3Progress.animateTo(1f, androidx.compose.animation.core.tween(150, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        }
        launch {
            kotlinx.coroutines.delay(560)
            line4Progress.animateTo(1f, androidx.compose.animation.core.tween(150, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        }
        launch {
            kotlinx.coroutines.delay(630)
            line5Progress.animateTo(1f, androidx.compose.animation.core.tween(150, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        }

        kotlinx.coroutines.delay(900)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth(0.3f).aspectRatio(1f)) {
            val scaleX = size.width / 100f
            val scaleY = size.height / 100f
            
            val outlinePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(20f * scaleX, 10f * scaleY)
                lineTo(80f * scaleX, 10f * scaleY)
                lineTo(80f * scaleX, 80f * scaleY)
                lineTo(75f * scaleX, 90f * scaleY)
                lineTo(70f * scaleX, 80f * scaleY)
                lineTo(65f * scaleX, 90f * scaleY)
                lineTo(60f * scaleX, 80f * scaleY)
                lineTo(55f * scaleX, 90f * scaleY)
                lineTo(50f * scaleX, 80f * scaleY)
                lineTo(45f * scaleX, 90f * scaleY)
                lineTo(40f * scaleX, 80f * scaleY)
                lineTo(35f * scaleX, 90f * scaleY)
                lineTo(30f * scaleX, 80f * scaleY)
                lineTo(25f * scaleX, 90f * scaleY)
                lineTo(20f * scaleX, 80f * scaleY)
                close()
            }
            
            val pm = android.graphics.PathMeasure()
            
            fun drawTrimmedPath(path: androidx.compose.ui.graphics.Path, progress: Float) {
                if (progress <= 0f) return
                pm.setPath(path.asAndroidPath(), false)
                val dst = android.graphics.Path()
                pm.getSegment(0f, pm.length * progress, dst, true)
                drawPath(
                    path = dst.asComposePath(),
                    color = androidx.compose.ui.graphics.Color.White,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 5f * scaleX,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
            
            drawTrimmedPath(outlinePath, outlineProgress.value)
            
            val lineYPositions = listOf(22f, 34f, 46f, 58f, 70f)
            val lineProgresses = listOf(line1Progress, line2Progress, line3Progress, line4Progress, line5Progress)
            
            for (i in 0 until 5) {
                val y = lineYPositions[i]
                val linePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(30f * scaleX, y * scaleY)
                    lineTo(70f * scaleX, y * scaleY)
                }
                drawTrimmedPath(linePath, lineProgresses[i].value)
            }
        }
    }
}
