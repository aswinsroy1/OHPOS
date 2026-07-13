package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.combinedClickable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.theme.AppTheme
import com.example.data.MenuItem
import com.example.ui.components.PremiumMotion

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MenuScreen(
    selectedTab: NavItem = NavItem.Menu,
    onTabSelected: (NavItem) -> Unit = {},
    onMenuClick: () -> Unit = {},
    onReportsLongClick: (() -> Unit)? = null,
    showReportsDot: Boolean = false,
    viewModel: BillingViewModel = viewModel()
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    val dynamicTopPadding = 68.dp + (screenHeight * 0.02f)
    val spacerMedium = screenHeight * 0.02f
    val spacerLarge = screenHeight * 0.03f

    val menuItems by viewModel.allMenuItems.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val dynamicCategories by viewModel.categories.collectAsState()
    val categories = dynamicCategories.map { if (it == "All") "All Items" else it }
    var selectedCategory by remember { mutableStateOf("All Items") }
    
    
    var showAddItemForm by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<MenuItem?>(null) }
    var formHasUnsavedChanges by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showSuccessToast by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showImportSummary by remember { mutableStateOf(false) }
    var importSummaryText by remember { mutableStateOf("") }
    val isImportingPdf by viewModel.isImportingPdf.collectAsState()
    val pdfParsedItems by viewModel.pdfParsedItems.collectAsState()
    val importError by viewModel.importError.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    
    val pdfPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importFromPdf(context, it) }
    }
    
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importFromImages(context, uris)
        }
    }

    var tempCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var capturedImageUris by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }
    var showCameraActionDialog by remember { mutableStateOf(false) }

    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val newList = capturedImageUris.toMutableList()
            newList.add(tempCameraUri!!)
            capturedImageUris = newList
            showCameraActionDialog = true
        }
    }
    
    var itemToDelete by remember { mutableStateOf<MenuItem?>(null) }
    var showDeleteToast by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = showAddItemForm || itemToEdit != null || pdfParsedItems.isNotEmpty()) {
        if (pdfParsedItems.isNotEmpty()) {
            viewModel.clearPdfParsedItems()
        } else if (formHasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            showAddItemForm = false
            itemToEdit = null
        }
    }
    
    androidx.compose.runtime.LaunchedEffect(showDeleteToast) {
        if (showDeleteToast) {
            kotlinx.coroutines.delay(2000)
            showDeleteToast = false
        }
    }
    
    androidx.compose.runtime.LaunchedEffect(showSuccessToast) {
        if (showSuccessToast) {
            kotlinx.coroutines.delay(2000)
            showSuccessToast = false
        }
    }
    
    val availableCategories = remember(menuItems) {
        (categories.drop(1) + menuItems.map { it.category }).distinct().filter { it.isNotBlank() }
    }
    var selectedItemForAction by remember { mutableStateOf<MenuItem?>(null) }
    
    val filteredItems = remember(menuItems, searchQuery, selectedCategory) {
        val filtered = menuItems.filter {
            (selectedCategory == "All Items" || it.category.equals(selectedCategory, ignoreCase = true)) &&
            (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
        }
        filtered.sortedBy { !it.isActive }
    }

    PremiumModalOverlay(
        isVisible = selectedItemForAction != null || showAddItemForm || showFabMenu || pdfParsedItems.isNotEmpty(),
        onDismissRequest = { 
            if (selectedItemForAction != null) {
                selectedItemForAction = null 
            } else if (showAddItemForm) {
                if (formHasUnsavedChanges) showUnsavedDialog = true
                else showAddItemForm = false
            }
        },
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                AppScaffold(
                    topBar = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppTheme.colors.background)
                        ) {
                            AppTopBar(
                                title = "Menu",
                                onMenuClick = onMenuClick,
                                modifier = Modifier
                                    .windowInsetsPadding(WindowInsets.statusBars)
                                    .padding(horizontal = AppTheme.spacing.lg)
                            )
                        }
                    },
                    bottomBar = {
                    }
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 0.dp), // Padding inside items
                        contentPadding = PaddingValues(
                            top = dynamicTopPadding + WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                            bottom = 110.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 80.dp // Extra padding for FAB
                        ),
                        verticalArrangement = Arrangement.spacedBy(spacerMedium)
                    ) {
                        item(key = "search_bar") {
                            AppSearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                placeholder = "Search menu...",
                                trailingIcon = Icons.Rounded.FilterList,
                                modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
                            )
                        }
                        
                        item(key = "categories") {
                            CategoryChips(
                                categories = categories,
                                selectedCategory = selectedCategory,
                                onCategorySelected = { selectedCategory = it }
                            )
                        }
                        
                        if (filteredItems.isEmpty()) {
                            item(key = "empty_state") {
                                Spacer(modifier = Modifier.height(spacerLarge * 2))
                                EmptyState(
                                    title = "No menu items yet",
                                    subtitle = "Tap + to add your first menu item.",
                                    icon = Icons.Rounded.RestaurantMenu
                                )
                            }
                        } else {
                            items(filteredItems, key = { it.id }) { item ->
                                MenuManagementCard(
                                    menuItem = item,
                                    onClick = { 
                                        itemToEdit = item
                                        showAddItemForm = true 
                                    },
                                    onLongPress = { selectedItemForAction = item },
                                    modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
                                )
                            }
                        }
                    }
                }
                // Success Toast
                AnimatedVisibility(
                    visible = showSuccessToast,
                    enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + androidx.compose.animation.slideInVertically(initialOffsetY = { -it }),
                    exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = dynamicTopPadding + 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(AppTheme.radius.full)
                            .background(AppTheme.colors.surface)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), AppTheme.radius.full)
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text("Menu item added", style = AppTheme.typography.labelMedium, color = AppTheme.colors.textPrimary)
                    }
                }
                // Delete Toast
                AnimatedVisibility(
                    visible = showDeleteToast,
                    enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + androidx.compose.animation.slideInVertically(initialOffsetY = { -it }),
                    exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = dynamicTopPadding + 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(AppTheme.radius.full)
                            .background(AppTheme.colors.surface)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), AppTheme.radius.full)
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text("Menu item deleted", style = AppTheme.typography.labelMedium, color = AppTheme.colors.textPrimary)
                    }
                }


                
                // FAB
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = AppTheme.spacing.lg, bottom = 110.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                ) {
                    androidx.compose.material3.Surface(
                        shape = AppTheme.radius.full,
                        color = AppTheme.colors.accent,
                        contentColor = AppTheme.colors.background,
                        shadowElevation = 6.dp,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .combinedClickable(
                                    onClick = { 
                                        itemToEdit = null
                                        showAddItemForm = true
                                    },
                                    onLongClick = {
                                        view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                                        showFabMenu = true
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add Menu Item",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        },
        overlayContent = {
            // PDF Loading Overlay
            if (showCameraActionDialog) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { /* Force explicit action */ },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppTheme.spacing.lg)
                            .clip(AppTheme.radius.xl)
                            .background(AppTheme.colors.surface)
                            .border(1.dp, AppTheme.colors.borderLight, AppTheme.radius.xl)
                            .padding(AppTheme.spacing.xl)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Page Captured",
                                style = AppTheme.typography.titleLarge,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                            Text(
                                text = "You have captured ${capturedImageUris.size} page(s). What would you like to do?",
                                style = AppTheme.typography.bodyLarge,
                                color = AppTheme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                            
                            com.example.ui.components.PrimaryButton(
                                text = "Finish & Import",
                                onClick = {
                                    showCameraActionDialog = false
                                    viewModel.importFromImages(context, capturedImageUris)
                                    capturedImageUris = emptyList()
                                }
                            )
                            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(AppTheme.radius.md)
                                    .border(1.dp, AppTheme.colors.borderLight, AppTheme.radius.md)
                                    .liftClickable {
                                        showCameraActionDialog = false
                                        val uri = com.example.util.CameraUtil.createImageUri(context)
                                        tempCameraUri = uri
                                        cameraLauncher.launch(uri)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Add Another",
                                    style = AppTheme.typography.titleMedium,
                                    color = AppTheme.colors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(AppTheme.radius.md)
                                    .liftClickable {
                                        if (capturedImageUris.isNotEmpty()) {
                                            capturedImageUris = capturedImageUris.dropLast(1)
                                        }
                                        showCameraActionDialog = false
                                        val uri = com.example.util.CameraUtil.createImageUri(context)
                                        tempCameraUri = uri
                                        cameraLauncher.launch(uri)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Retake",
                                    style = AppTheme.typography.titleMedium,
                                    color = AppTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isImportingPdf,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppTheme.colors.background.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        androidx.compose.material3.CircularProgressIndicator(color = AppTheme.colors.accent)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Analyzing Document...", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textPrimary)
                    }
                }
            }

            // PDF Import Review
            if (pdfParsedItems.isNotEmpty()) {
                PdfImportReviewOverlay(
                    isVisible = true,
                    parsedItems = pdfParsedItems,
                    existingItems = menuItems,
                    onDismissRequest = { viewModel.clearPdfParsedItems() },
                    onItemChange = { index, item -> viewModel.updateParsedItem(index, item) },
                    onImportConfirmed = { actions ->
                        var imported = 0
                        var skipped = 0
                        var replaced = 0
                        
                        actions.forEach { action ->
                            when (action.type) {
                                ImportActionType.SKIP -> skipped++
                                ImportActionType.REPLACE -> {
                                    viewModel.saveMenuItem(action.item.copy(id = action.existingId))
                                    replaced++
                                }
                                ImportActionType.IMPORT -> {
                                    viewModel.saveMenuItem(action.item.copy(id = 0))
                                    imported++
                                }
                            }
                        }
                        
                        viewModel.clearPdfParsedItems()
                        importSummaryText = "Imported ${imported + replaced} items\nSkipped $skipped duplicates"
                        showImportSummary = true
                    }
                )
            }

            // FAB Menu Popup
            AnimatedVisibility(
                visible = showFabMenu,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showFabMenu = false },
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Column(
                        modifier = Modifier
                            .padding(end = AppTheme.spacing.lg, bottom = 180.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                            .width(220.dp)
                            .clip(AppTheme.radius.lg)
                            .background(AppTheme.colors.surface)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), AppTheme.radius.lg)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {}
                    ) {
                        ActionPanelItem(
                            icon = Icons.Rounded.PictureAsPdf,
                            text = "Import Menu from PDF",
                            onClick = {
                                showFabMenu = false
                                pdfPickerLauncher.launch("application/pdf")
                            }
                        )
                        ActionPanelItem(
                            icon = Icons.Rounded.Image,
                            text = "Import Menu from Images",
                            onClick = {
                                showFabMenu = false
                                imagePickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                        )
                        ActionPanelItem(
                            icon = Icons.Rounded.CameraAlt,
                            text = "Scan Menu with Camera",
                            onClick = {
                                showFabMenu = false
                                capturedImageUris = emptyList()
                                val uri = com.example.util.CameraUtil.createImageUri(context)
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            }
                        )
                    }
                }
            }
            
            AnimatedVisibility(
                visible = selectedItemForAction != null,
                enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()),
                exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()),
                modifier = Modifier.align(Alignment.Center)
            ) {
                selectedItemForAction?.let { item ->
                    Box(
                        modifier = Modifier
                            .padding(AppTheme.spacing.lg)
                            .clip(AppTheme.radius.lg)
                            .background(AppTheme.colors.surface)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {}
                            .padding(vertical = AppTheme.spacing.md)
                            .width(260.dp)
                    ) {
                        Column {
                            ActionPanelItem(
                                icon = Icons.Rounded.Edit,
                                text = "Edit Item",
                                onClick = { 
                                    itemToEdit = item
                                    showAddItemForm = true
                                    selectedItemForAction = null
                                }
                            )
                            ActionPanelItem(
                                icon = Icons.Rounded.ContentCopy,
                                text = "Duplicate Item",
                                onClick = { 
                                    val duplicate = item.copy(id = 0, name = "${item.name} (Copy)")
                                    viewModel.saveMenuItem(duplicate)
                                    selectedItemForAction = null
                                }
                            )
                            ActionPanelItem(
                                icon = if (item.isActive) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                text = if (item.isActive) "Temporarily Disable" else "Enable Item",
                                onClick = { 
                                    viewModel.updateMenuAvailability(item.id, !item.isActive)
                                    selectedItemForAction = null
                                }
                            )
                            ActionPanelItem(
                                icon = Icons.Rounded.Delete,
                                text = "Delete Item",
                                textColor = Color(0xFFFF4C4C),
                                iconColor = Color(0xFFFF4C4C),
                                onClick = { 
                                    itemToDelete = item
                                    selectedItemForAction = null
                                }
                            )
                        }
                    }
                }
            }
            
            AnimatedVisibility(
                visible = showAddItemForm,
                enter = androidx.compose.animation.slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = PremiumMotion.defaultSpring()
                ),
                exit = androidx.compose.animation.slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = PremiumMotion.defaultSpring()
                ),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                // key() forces Compose to recreate the entire subtree — and therefore all
                // remembered state inside AddMenuItemForm — whenever the form is opened for
                // a different item (or opened fresh via the "+" button with itemToEdit = null).
                // Without this, AnimatedVisibility keeps the old composition alive between
                // opens, so remember { } never re-runs and the previous field values persist.
                key(itemToEdit?.id ?: -1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.9f)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(AppTheme.colors.background)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {}
                    ) {
                        AddMenuItemForm(
                            initialItem = itemToEdit,
                            existingCategories = availableCategories,
                            onHasUnsavedChanges = { formHasUnsavedChanges = it },
                            onSave = { newItem ->
                                viewModel.saveMenuItem(newItem)
                                showAddItemForm = false
                                itemToEdit = null
                                formHasUnsavedChanges = false
                                if (newItem.id == 0) showSuccessToast = true
                            },
                            onCancel = {
                                if (formHasUnsavedChanges) showUnsavedDialog = true
                                else {
                                    showAddItemForm = false
                                    itemToEdit = null
                                }
                            }
                        )
                    }
                }
            }
        }
    )

    // Import Summary Dialog
    if (showImportSummary) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showImportSummary = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.spacing.lg)
                    .clip(AppTheme.radius.xl)
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.borderLight, AppTheme.radius.xl)
                    .padding(AppTheme.spacing.xl)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Import Complete",
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                    Text(
                        text = importSummaryText,
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    
                    com.example.ui.components.PrimaryButton(
                        text = "OK",
                        onClick = { showImportSummary = false }
                    )
                }
            }
        }
    }

    if (importError != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewModel.clearImportError() },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.spacing.lg)
                    .clip(AppTheme.radius.xl)
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.borderLight, AppTheme.radius.xl)
                    .padding(AppTheme.spacing.xl)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (importError?.contains("No menu items") == true) "No Menu Items Found" else "Import Failed",
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                    Text(
                        text = importError ?: "",
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    com.example.ui.components.PrimaryButton(
                        text = "OK",
                        onClick = { viewModel.clearImportError() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showUnsavedDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showUnsavedDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.spacing.lg)
                    .clip(AppTheme.radius.xl)
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.borderLight, AppTheme.radius.xl)
                    .padding(AppTheme.spacing.xl)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Discard Changes?",
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                    Text(
                        text = "You have unsaved changes. Are you sure you want to discard them?",
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    
                    com.example.ui.components.PrimaryButton(
                        text = "Discard",
                        containerColor = androidx.compose.ui.graphics.Color(0xFFFF4C4C),
                        onClick = {
                            showUnsavedDialog = false
                            showAddItemForm = false
                            itemToEdit = null
                            formHasUnsavedChanges = false
                        }
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(AppTheme.radius.md)
                            .border(1.dp, AppTheme.colors.borderLight, AppTheme.radius.md)
                            .liftClickable { showUnsavedDialog = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Continue Editing",
                            style = AppTheme.typography.titleMedium,
                            color = AppTheme.colors.textPrimary
                        )
                    }
                }
            }
        }
    }
    if (itemToDelete != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { itemToDelete = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.spacing.lg)
                    .clip(AppTheme.radius.xl)
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.borderLight, AppTheme.radius.xl)
                    .padding(AppTheme.spacing.xl)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Delete Item?",
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                    Text(
                        text = "This action cannot be undone.",
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    
                    com.example.ui.components.PrimaryButton(
                        text = "Delete",
                        containerColor = androidx.compose.ui.graphics.Color(0xFFFF4C4C),
                        onClick = {
                            itemToDelete?.let {
                                viewModel.deleteMenuItem(it.id, it.imageUrl)
                                showDeleteToast = true
                            }
                            itemToDelete = null
                        }
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(AppTheme.radius.md)
                            .border(1.dp, AppTheme.colors.borderLight, AppTheme.radius.md)
                            .liftClickable { itemToDelete = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            style = AppTheme.typography.titleMedium,
                            color = AppTheme.colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionPanelItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    textColor: Color = AppTheme.colors.textPrimary,
    iconColor: Color = AppTheme.colors.textSecondary,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable { onClick() }
            .padding(horizontal = AppTheme.spacing.lg, vertical = 14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = AppTheme.typography.bodyLarge,
            color = textColor
        )
    }
}
