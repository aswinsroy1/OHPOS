import re

with open("app/src/main/java/com/example/ui/screens/MenuScreen.kt", "r") as f:
    content = f.read()

# 1. Add states for PDF import
pdf_states = """    var showSuccessToast by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showImportSummary by remember { mutableStateOf(false) }
    var importSummaryText by remember { mutableStateOf("") }
    val isImportingPdf by viewModel.isImportingPdf.collectAsState()
    val pdfParsedItems by viewModel.pdfParsedItems.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val pdfPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importFromPdf(context, it) }
    }"""
content = content.replace("    var showSuccessToast by remember { mutableStateOf(false) }", pdf_states)

# 2. Add loading state and review overlay at the end of the main Box
# We should put it near PremiumModalOverlay
overlays = """
    // PDF Loading Overlay
    AnimatedVisibility(
        visible = isImportingPdf,
        enter = fadeIn(),
        exit = fadeOut()
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
                Text("Analyzing PDF...", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textPrimary)
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
                importSummaryText = "Imported ${imported + replaced} items\\nSkipped $skipped duplicates"
                showImportSummary = true
            }
        )
    }

    // Import Summary Dialog
    if (showImportSummary) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showImportSummary = false },
            title = { androidx.compose.material3.Text("Import Complete", color = AppTheme.colors.textPrimary) },
            text = { androidx.compose.material3.Text(importSummaryText, color = AppTheme.colors.textSecondary) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showImportSummary = false }) {
                    androidx.compose.material3.Text("OK", color = AppTheme.colors.accent)
                }
            },
            containerColor = AppTheme.colors.surface
        )
    }

"""

# We'll inject these before PremiumModalOverlay
content = content.replace("    PremiumModalOverlay(", overlays + "    PremiumModalOverlay(")

# 3. Update the FAB block
fab_replacement = """                // FAB
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
                                .androidx.compose.foundation.ExperimentalFoundationApi::class
                                .let {
                                    @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                                    Modifier.combinedClickable(
                                        onClick = {
                                            itemToEdit = null
                                            showAddItemForm = true
                                        },
                                        onLongClick = {
                                            androidx.compose.ui.platform.LocalView.current.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                                            showFabMenu = true
                                        }
                                    )
                                },
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
"""

# Try matching the exact existing FAB code
fab_pattern = r'// FAB\s*FloatingActionButton\([\s\S]*?Icon\([\s\S]*?\}\s*\)\s*\}'
content = re.sub(fab_pattern, fab_replacement, content)

# 4. Add the small premium context menu for FAB
fab_menu = """
    PremiumModalOverlay(
        isVisible = showFabMenu,
        onDismissRequest = { showFabMenu = false }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                modifier = Modifier
                    .padding(end = AppTheme.spacing.lg, bottom = 180.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                    .width(220.dp)
                    .clip(AppTheme.radius.lg)
                    .background(AppTheme.colors.surface)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), AppTheme.radius.lg)
            ) {
                ActionPanelItem(
                    icon = Icons.Rounded.PictureAsPdf,
                    text = "Import Menu from PDF",
                    onClick = {
                        showFabMenu = false
                        pdfPickerLauncher.launch("application/pdf")
                    }
                )
            }
        }
    }
"""
content = content.replace("    if (itemToDelete != null) {", fab_menu + "\n    if (itemToDelete != null) {")


with open("app/src/main/java/com/example/ui/screens/MenuScreen.kt", "w") as f:
    f.write(content)
