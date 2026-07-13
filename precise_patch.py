import re

with open("app/src/main/java/com/example/ui/screens/MenuScreen.kt", "r") as f:
    content = f.read()

# 1. Imports
if "import androidx.compose.ui.zIndex.zIndex" not in content:
    content = content.replace("import androidx.compose.ui.unit.dp", "import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.zIndex.zIndex")

# 2. OptIn
if "@OptIn" not in content:
    content = content.replace("@Composable\nfun MenuScreen", "@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)\n@Composable\nfun MenuScreen")

# 3. State variables
states = """    var showSuccessToast by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    var showImportSummary by remember { mutableStateOf(false) }
    var importSummaryText by remember { mutableStateOf("") }
    val isImportingPdf by viewModel.isImportingPdf.collectAsState()
    val pdfParsedItems by viewModel.pdfParsedItems.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    
    val pdfPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importFromPdf(context, it) }
    }"""
content = content.replace("    var showSuccessToast by remember { mutableStateOf(false) }", states)

# 4. Overlays inside overlayContent
overlays = """
            // PDF Loading Overlay
            AnimatedVisibility(
                visible = isImportingPdf,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.zIndex(100f)
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
                    }
                }
            }
"""

content = content.replace("        overlayContent = {\n", "        overlayContent = {\n" + overlays)

# 5. Fix FAB
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
                                .androidx.compose.foundation.combinedClickable(
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
                }"""

fab_pattern = r'(\s*)// FAB\s*FloatingActionButton\([\s\S]*?Icon\([\s\S]*?\}\s*\)\s*\}'
content = re.sub(fab_pattern, "\n" + fab_replacement, content)

with open("app/src/main/java/com/example/ui/screens/MenuScreen.kt", "w") as f:
    f.write(content)
