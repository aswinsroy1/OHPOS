package com.example.ui.screens
import com.example.ui.screens.PrinterViewModel
import android.widget.Toast
import com.example.data.SavedPrinter


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import kotlinx.coroutines.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.components.AppSearchBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.components.liftClickable
import com.example.ui.theme.AppTheme
import com.example.util.PinManager
import com.example.ui.components.PinEntryDialog
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.rounded.Lock

data class SettingItemData(
    val title: String,
    val icon: ImageVector,
    val hasSwitch: Boolean = false,
    val switchState: Boolean = false,
    val onSwitchChange: ((Boolean) -> Unit)? = null,
    val subtitle: String? = null,
    val onClick: () -> Unit = {}
)

data class SettingSectionData(
    val title: String,
    val items: List<SettingItemData>
)

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToPrinters: () -> Unit = {},
    onNavigateToReceiptLayout: () -> Unit = {},
    onNavigateToAutoBackup: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val pinManager = remember { PinManager(context) }
    var showPinSetup by remember { mutableStateOf(false) }
    var showPaperSizeSheet by remember { mutableStateOf(false) }
    val prefRepo = remember { com.example.data.PrinterPreferencesRepository(context) }
    val currentPaperSize by prefRepo.paperSizeFlow.collectAsState(initial = 58)
    val scope = rememberCoroutineScope()

    var showThemeSheet by remember { mutableStateOf(false) }
    var showAccentSheet by remember { mutableStateOf(false) }
    val appearanceRepo = remember { com.example.data.AppearancePreferencesRepository(context) }
    val currentTheme by appearanceRepo.themeFlow.collectAsState(initial = "Dark")
    val currentAccent by appearanceRepo.accentColourFlow.collectAsState(initial = "Monochrome")

    
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreManifest by remember { mutableStateOf<com.example.util.BackupManifest?>(null) }
    var restoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isRestoring by remember { mutableStateOf(false) }
    
    val autoBackupPrefsRepo = remember { com.example.data.AutoBackupPreferencesRepository(context) }
    val isAutoBackupEnabled by autoBackupPrefsRepo.isAutoBackupEnabled.collectAsState(initial = false)
    val backupFrequencyHours by autoBackupPrefsRepo.backupFrequencyHours.collectAsState(initial = 24)

    val dailyClosingPrefRepo = remember { com.example.data.DailyClosingPreferencesRepository(context) }
    val isAutoCloseEnabled by dailyClosingPrefRepo.isAutoCloseEnabled.collectAsState(initial = false)
    val autoCloseHour by dailyClosingPrefRepo.autoCloseHour.collectAsState(initial = 23)
    val autoCloseMinute by dailyClosingPrefRepo.autoCloseMinute.collectAsState(initial = 59)
    val exportFolderUri by dailyClosingPrefRepo.exportFolderUri.collectAsState(initial = null)
    
    val reportFolderPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            scope.launch {
                dailyClosingPrefRepo.setExportFolderUri(uri.toString())
                Toast.makeText(context, "Export folder updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Notification preferences
    val notifPrefsRepo = remember { com.example.data.NotificationPreferencesRepository(context) }
    val notifyBackupSuccess by notifPrefsRepo.backupSuccessEnabled.collectAsState(initial = true)
    val notifyBackupFailure by notifPrefsRepo.backupFailureEnabled.collectAsState(initial = true)
    val notifyPrinterFailure by notifPrefsRepo.printerFailureEnabled.collectAsState(initial = true)
    val notifyDeletionRequest by notifPrefsRepo.deletionRequestEnabled.collectAsState(initial = true)

    // POST_NOTIFICATIONS permission launcher (Android 13+)
    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — toggles are already saved, notifications will just be silently skipped if denied */ }
    
    val createBackupLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = com.example.util.BackupRestoreManager.performBackup(context, uri)
                if (success) {
                    Toast.makeText(context, "Backup completed successfully.", Toast.LENGTH_LONG).show()
                    com.example.util.AppNotificationManager.notifyBackupSuccess(context, "Manual backup completed successfully")
                } else {
                    Toast.makeText(context, "Backup failed.", Toast.LENGTH_LONG).show()
                    com.example.util.AppNotificationManager.notifyBackupFailure(context, "Manual backup failed")
                }
            }
        }
    }
    
    val backupBeforeRestoreLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = com.example.util.BackupRestoreManager.performBackup(context, uri)
                if (success) {
                    Toast.makeText(context, "Pre-restore Backup completed.", Toast.LENGTH_SHORT).show()
                    showRestoreDialog = true
                } else {
                    Toast.makeText(context, "Pre-restore Backup failed.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    val restoreBackupLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = com.example.util.BackupRestoreManager.inspectBackup(context, uri)
                if (result.isSuccess) {
                    restoreManifest = result.getOrNull()
                    restoreUri = uri
                    showRestoreDialog = true
                } else {
                    Toast.makeText(context, "Restore failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    

    val printerViewModel: PrinterViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val savedPrinters by printerViewModel.savedPrinters.collectAsState()
    val isPrinting by printerViewModel.isPrinting.collectAsState()
    val printError by printerViewModel.printError.collectAsState()
    
    LaunchedEffect(printError) {
        if (printError != null) {
            Toast.makeText(context, printError, Toast.LENGTH_LONG).show()
            printerViewModel.clearError()
        }
    }
    

    val billingViewModel: com.example.ui.screens.BillingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val reportsViewModel: com.example.ui.screens.ReportsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val pdfParsedItems by billingViewModel.pdfParsedItems.collectAsState()
    val isImportingPdf by billingViewModel.isImportingPdf.collectAsState()
    val importError by billingViewModel.importError.collectAsState()
    val menuItems by billingViewModel.allMenuItems.collectAsState()
    val allBills by reportsViewModel.allBills.collectAsState()
    val resName by prefRepo.resNameFlow.collectAsState(initial = "OH POS")
    
    LaunchedEffect(importError) {
        importError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            billingViewModel.clearImportError()
        }
    }

    val importPdfLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { billingViewModel.importFromPdf(context, it) }
    }

    var showMenuExportSuccess by remember { mutableStateOf(false) }
    val exportMenuLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
                    val jsonString = gson.toJson(menuItems)
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }
                    showMenuExportSuccess = true
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var showExportReportsDialog by remember { mutableStateOf(false) }
    var selectedReportFilter by remember { mutableStateOf("Today's Report") }
    var showReportExportSuccess by remember { mutableStateOf(false) }
    
    val exportReportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    context.contentResolver.openOutputStream(uri)?.let { outputStream ->
                        val success = reportsViewModel.generateCustomReport(context, outputStream, selectedReportFilter, resName)
                        if (success) {
                            showReportExportSuccess = true
                        } else {
                            Toast.makeText(context, "Report generation failed.", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val allSections = listOf(

        SettingSectionData(
            title = "Appearance",
            items = listOf(
                SettingItemData("Theme", Icons.Rounded.Palette, onClick = { showThemeSheet = true }),
                SettingItemData("Accent Colour", Icons.Rounded.ColorLens, onClick = { showAccentSheet = true })
            )
        ),
        SettingSectionData(
            title = "Printing",
            items = listOf(
                SettingItemData("Printer Settings", Icons.Rounded.Print, onClick = onNavigateToPrinters),
                SettingItemData("Receipt Layout", Icons.Rounded.ListAlt, onClick = onNavigateToReceiptLayout),
                SettingItemData("Paper Size", Icons.Rounded.InsertDriveFile, onClick = { showPaperSizeSheet = true }),
                SettingItemData("Test Print", Icons.Rounded.DoneAll, onClick = { val printer = savedPrinters.firstOrNull { it.isDefault } ?: savedPrinters.firstOrNull(); if (printer != null) printerViewModel.testPrint(context, printer) else Toast.makeText(context, "No printer configured.", Toast.LENGTH_LONG).show() })
            )
        ),
        SettingSectionData(
            title = "Daily Closing",
            items = listOf(
                SettingItemData(
                    title = "Automatically close each day",
                    icon = Icons.Rounded.EventAvailable,
                    hasSwitch = true,
                    switchState = isAutoCloseEnabled,
                    onSwitchChange = { checked -> scope.launch { dailyClosingPrefRepo.setAutoCloseEnabled(checked) } }
                ),
                SettingItemData(
                    title = "Auto-close time",
                    icon = Icons.Rounded.Schedule,
                    subtitle = String.format("%02d:%02d", autoCloseHour, autoCloseMinute),
                    onClick = {
                        if (isAutoCloseEnabled) {
                            android.app.TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    scope.launch {
                                        dailyClosingPrefRepo.setAutoCloseHour(hour)
                                        dailyClosingPrefRepo.setAutoCloseMinute(minute)
                                    }
                                },
                                autoCloseHour,
                                autoCloseMinute,
                                true
                            ).show()
                        } else {
                            Toast.makeText(context, "Enable automatic closing first.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ),
                SettingItemData(
                    title = "Export folder for daily reports",
                    icon = Icons.Rounded.FolderOpen,
                    subtitle = exportFolderUri?.let { android.net.Uri.parse(it).lastPathSegment } ?: "Not set",
                    onClick = {
                        reportFolderPicker.launch(null)
                    }
                )
            )
        ),
        SettingSectionData(
            title = "Backup",
            items = listOf(
                SettingItemData("Backup Now", Icons.Rounded.Backup, onClick = { createBackupLauncher.launch("OH_POS_Backup_${java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm", java.util.Locale.US).format(java.util.Date())}.zip") }),
                SettingItemData("Restore Backup", Icons.Rounded.SettingsBackupRestore, onClick = { restoreBackupLauncher.launch(arrayOf("application/zip")) }),
                SettingItemData(
                    title = "Automatic Backups (${if (isAutoBackupEnabled) "ON" else "OFF"})",
                    icon = Icons.Rounded.Sync,
                    hasSwitch = true,
                    switchState = isAutoBackupEnabled,
                    onSwitchChange = { checked ->
                        scope.launch {
                            autoBackupPrefsRepo.setAutoBackupEnabled(checked)
                            if (checked) {
                                com.example.util.AutoBackupScheduler.schedule(context, backupFrequencyHours)
                            } else {
                                com.example.util.AutoBackupScheduler.cancel(context)
                            }
                            Toast.makeText(context, "Automatic Backup ${if (checked) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onClick = onNavigateToAutoBackup
                )
            )
        ),
        SettingSectionData(
            title = "Notifications",
            items = listOf(
                SettingItemData(
                    title = "Backup Complete",
                    icon = Icons.Rounded.CloudDone,
                    hasSwitch = true,
                    switchState = notifyBackupSuccess,
                    onSwitchChange = { checked ->
                        scope.launch { notifPrefsRepo.setBackupSuccessEnabled(checked) }
                        if (checked && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                            !com.example.util.AppNotificationManager.hasNotificationPermission(context)) {
                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                ),
                SettingItemData(
                    title = "Backup Failed",
                    icon = Icons.Rounded.CloudOff,
                    hasSwitch = true,
                    switchState = notifyBackupFailure,
                    onSwitchChange = { checked ->
                        scope.launch { notifPrefsRepo.setBackupFailureEnabled(checked) }
                        if (checked && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                            !com.example.util.AppNotificationManager.hasNotificationPermission(context)) {
                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                ),
                SettingItemData(
                    title = "Print Failure",
                    icon = Icons.Rounded.PrintDisabled,
                    hasSwitch = true,
                    switchState = notifyPrinterFailure,
                    onSwitchChange = { checked ->
                        scope.launch { notifPrefsRepo.setPrinterFailureEnabled(checked) }
                        if (checked && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                            !com.example.util.AppNotificationManager.hasNotificationPermission(context)) {
                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                ),
                SettingItemData(
                    title = "Deletion Requests",
                    icon = Icons.Rounded.DeleteSweep,
                    hasSwitch = true,
                    switchState = notifyDeletionRequest,
                    onSwitchChange = { checked ->
                        scope.launch { notifPrefsRepo.setDeletionRequestEnabled(checked) }
                        if (checked && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                            !com.example.util.AppNotificationManager.hasNotificationPermission(context)) {
                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
            )
        ),
        SettingSectionData(
            title = "Import & Export",
            items = listOf(
                SettingItemData("Import Menu PDF", Icons.Rounded.PictureAsPdf, onClick = { importPdfLauncher.launch("application/pdf") }),
                SettingItemData("Export Menu", Icons.Rounded.FileDownload, onClick = {
                    if (menuItems.isEmpty()) {
                        Toast.makeText(context, "No menu items available to export.", Toast.LENGTH_LONG).show()
                    } else {
                        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                        exportMenuLauncher.launch("Menu_Backup_$date.json")
                    }
                }),
                SettingItemData("Export Reports", Icons.Rounded.Assessment, onClick = {
                    showExportReportsDialog = true
                })
            )
        )
    )
    
    val filteredSections = if (searchQuery.isBlank()) {
        allSections
    } else {
        allSections.mapNotNull { section ->
            val filteredItems = section.items.filter { 
                it.title.contains(searchQuery, ignoreCase = true) 
            }
            if (filteredItems.isNotEmpty()) {
                section.copy(items = filteredItems)
            } else null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            AppTopBar(
                title = "Settings",
                onBackClick = onBackClick,
                showTrailingIcon = false,
                modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
            )
            
            AppSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search settings...",
                trailingIcon = if (searchQuery.isNotEmpty()) Icons.Rounded.Clear else Icons.Rounded.Search,
                modifier = Modifier.padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md)
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = AppTheme.spacing.lg,
                    end = AppTheme.spacing.lg,
                    bottom = 120.dp
                )
            ) {
                filteredSections.forEach { section ->
                    item(key = section.title) {
                        SectionHeader(title = section.title)
                    }
                    
                    item(key = section.title + "_card") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = AppTheme.elevations.sm,
                                    shape = AppTheme.radius.lg,
                                    spotColor = Color.Black,
                                    ambientColor = Color.Black
                                )
                                .clip(AppTheme.radius.lg)
                                .background(AppTheme.colors.surface)
                                .border(AppTheme.dimensions.borderThickness, Color.White.copy(alpha = 0.06f), AppTheme.radius.lg)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                section.items.forEachIndexed { index, item ->
                                    SettingsRow(
                                        title = item.title,
                                        icon = item.icon,
                                        hasSwitch = item.hasSwitch,
                                        switchState = item.switchState,
                                        onSwitchChange = item.onSwitchChange,
                                        subtitle = item.subtitle,
                                        onClick = item.onClick
                                    )
                                    if (index < section.items.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(AppTheme.colors.divider)
                                                .padding(start = 56.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        PinEntryDialog(
            isVisible = showPinSetup,
            title = if (pinManager.hasPin()) "Update PIN" else "Setup PIN",
            subtitle = if (pinManager.hasPin()) "Enter new 4-digit PIN" else "Create a 4-digit PIN for Deletion Approvals",
            isSetupMode = true,
            onPinEntered = { newPin ->
                pinManager.setPin(newPin)
                showPinSetup = false
            },
            onCancel = { showPinSetup = false }
        )
        
        if (showRestoreDialog && restoreManifest != null) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { if (!isRestoring) showRestoreDialog = false },
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
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Restore Backup?",
                            style = AppTheme.typography.titleLarge,
                            color = AppTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                        
                        Text(
                            text = "This backup contains:",
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                        
                        Column(modifier = Modifier.padding(start = AppTheme.spacing.sm)) {
                            Text("• Restaurant Name: ${restoreManifest?.restaurantName}", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                            Text("• Backup Date: ${restoreManifest?.created}", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                            Text("• Menu Item Count: ${restoreManifest?.menuItems}", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                            Text("• Order Count: ${restoreManifest?.orders}", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                            Text("• App Version: ${restoreManifest?.appVersion}", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                            Text("• Backup Version: ${restoreManifest?.backupVersion}", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                        }
                        
                        Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                        
                        Text(
                            text = "Restoring this backup will replace all current OH POS data.\n\nThis action cannot be undone.\n\nWould you like to create a backup before restoring?",
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.textSecondary
                        )
                        
                        if (isRestoring) {
                            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                color = AppTheme.colors.accent
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                        
                        com.example.ui.components.PrimaryButton(
                            text = "Backup First",
                            onClick = { 
                                if (!isRestoring) {
                                    backupBeforeRestoreLauncher.launch("OH_POS_Backup_${java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm", java.util.Locale.US).format(java.util.Date())}.zip")
                                }
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
                                    if (!isRestoring) {
                                        isRestoring = true
                                        scope.launch {
                                            val result = com.example.util.BackupRestoreManager.executeRestore(context, restoreUri!!)
                                            isRestoring = false
                                            showRestoreDialog = false
                                            if (result.isSuccess) {
                                                Toast.makeText(context, "Restore successful. Restarting app...", Toast.LENGTH_LONG).show()
                                                kotlinx.coroutines.delay(1500)
                                                if (context is android.app.Activity) {
                                                    val intent = context.intent
                                                    context.finish()
                                                    context.startActivity(intent)
                                                }
                                            } else {
                                                Toast.makeText(context, "Restore failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Restore Anyway",
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
                                    if (!isRestoring) showRestoreDialog = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
                                style = AppTheme.typography.titleMedium,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        if (showMenuExportSuccess) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showMenuExportSuccess = false },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { showMenuExportSuccess = false }) {
                        Text("OK", color = AppTheme.colors.accent)
                    }
                },
                title = { Text("Success", color = AppTheme.colors.textPrimary) },
                text = { Text("Menu exported successfully.", color = AppTheme.colors.textSecondary) },
                containerColor = AppTheme.colors.surface,
                titleContentColor = AppTheme.colors.textPrimary,
                textContentColor = AppTheme.colors.textSecondary
            )
        }

        if (showReportExportSuccess) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showReportExportSuccess = false },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { showReportExportSuccess = false }) {
                        Text("OK", color = AppTheme.colors.accent)
                    }
                },
                title = { Text("Success", color = AppTheme.colors.textPrimary) },
                text = { Text("Report exported successfully.", color = AppTheme.colors.textSecondary) },
                containerColor = AppTheme.colors.surface,
                titleContentColor = AppTheme.colors.textPrimary,
                textContentColor = AppTheme.colors.textSecondary
            )
        }

        if (showExportReportsDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showExportReportsDialog = false }
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
                    Column {
                        Text("Export Reports", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
                        Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                        val options = listOf("Today's Report", "Last 7 Days", "Last 30 Days", "Custom Date Range")
                        options.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(AppTheme.radius.md)
                                    .liftClickable {
                                        selectedReportFilter = option
                                    }
                                    .padding(vertical = AppTheme.spacing.md, horizontal = AppTheme.spacing.xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.RadioButton(
                                    selected = selectedReportFilter == option,
                                    onClick = { selectedReportFilter = option },
                                    colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                        selectedColor = AppTheme.colors.accent,
                                        unselectedColor = AppTheme.colors.textSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(option, style = AppTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            androidx.compose.material3.TextButton(onClick = { showExportReportsDialog = false }) {
                                Text("Cancel", color = AppTheme.colors.textSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.material3.Button(
                                onClick = {
                                    if (allBills.isEmpty()) {
                                        Toast.makeText(context, "No sales data available.", Toast.LENGTH_LONG).show()
                                    } else {
                                        showExportReportsDialog = false
                                        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                                        exportReportLauncher.launch("Sales_Report_$date.pdf")
                                    }
                                },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accent)
                            ) {
                                Text("Export", color = AppTheme.colors.background)
                            }
                        }
                    }
                }
            }
        }
        
        if (isImportingPdf) {
            androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
                Box(modifier = Modifier.padding(16.dp).clip(AppTheme.radius.lg).background(AppTheme.colors.surface).padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        androidx.compose.material3.CircularProgressIndicator(color = AppTheme.colors.accent)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Extracting menu items...", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textPrimary)
                    }
                }
            }
        }
        
        if (pdfParsedItems.isNotEmpty()) {
            com.example.ui.components.PdfImportReviewOverlay(
                isVisible = true,
                parsedItems = pdfParsedItems,
                existingItems = menuItems,
                onDismissRequest = { billingViewModel.clearPdfParsedItems() },
                onItemChange = { index, item -> billingViewModel.updateParsedItem(index, item) },
                onImportConfirmed = { actions ->
                    var imported = 0
                    var skipped = 0
                    var replaced = 0
                    scope.launch {
                        actions.forEach { action ->
                            when (action.type) {
                                com.example.ui.components.ImportActionType.SKIP -> skipped++
                                com.example.ui.components.ImportActionType.REPLACE -> {
                                    billingViewModel.saveMenuItem(action.item.copy(id = action.existingId))
                                    replaced++
                                }
                                com.example.ui.components.ImportActionType.IMPORT -> {
                                    billingViewModel.saveMenuItem(action.item.copy(id = 0))
                                    imported++
                                }
                            }
                        }
                        billingViewModel.clearPdfParsedItems()
                        Toast.makeText(context, "Imported ${imported + replaced} items\nSkipped $skipped duplicates", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        if (showThemeSheet) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showThemeSheet = false },
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
                        Text("Theme", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
                        Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                        Text("Choose the application's appearance.", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                        
                        val themes = listOf(
                            Triple("Dark", "Dark", "A dark theme for low-light environments."),
                            Triple("Pure Black", "Pure Black", "A true black theme for OLED displays."),
                            Triple("Light", "Light", "A bright theme for well-lit environments."),
                            Triple("System Default", "System Default", "Follows your device's system settings.")
                        )
                        
                        themes.forEachIndexed { index, (id, title, subtitle) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(AppTheme.radius.md)
                                    .liftClickable { 
                                        scope.launch { appearanceRepo.setThemePref(id); showThemeSheet = false } 
                                    }
                                    .padding(vertical = AppTheme.spacing.md, horizontal = AppTheme.spacing.xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.RadioButton(
                                    selected = currentTheme == id, 
                                    onClick = { scope.launch { appearanceRepo.setThemePref(id); showThemeSheet = false } },
                                    colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                        selectedColor = AppTheme.colors.accent,
                                        unselectedColor = AppTheme.colors.textSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(title, style = AppTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
                                    Text(subtitle, style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                                }
                            }
                            if (index < themes.size - 1) {
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppTheme.colors.divider))
                            }
                        }
                    }
                }
            }
        }

        if (showAccentSheet) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showAccentSheet = false },
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
                        Text("Accent Colour", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
                        Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                        Text("Choose the primary accent colour.", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                        
                        val isDarkTheme = AppTheme.colors.background == Color(0xFF1B1B1D)
                        val colours = listOf(
                            Triple("Monochrome", "Monochrome", if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF1B1B1D)),
                            Triple("Graphite", "Graphite", if (isDarkTheme) Color(0xFF9B9B9E) else Color(0xFF4A4A4D)),
                            Triple("Slate", "Slate", if (isDarkTheme) Color(0xFF9DB0BE) else Color(0xFF5C6773)),
                            Triple("Amber Brass", "Amber Brass", Color(0xFFB8863B)),
                            Triple("Sage", "Sage", Color(0xFF7C8C6E)),
                            Triple("Ink", "Ink", if (isDarkTheme) Color(0xFF7C8FA8) else Color(0xFF3C4858))
                        )
                        
                        colours.forEachIndexed { index, (id, title, colorVal) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(AppTheme.radius.md)
                                    .liftClickable { 
                                        scope.launch { appearanceRepo.setAccentColour(id); showAccentSheet = false } 
                                    }
                                    .padding(vertical = AppTheme.spacing.sm, horizontal = AppTheme.spacing.xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(colorVal)
                                        .border(2.dp, if (currentAccent == id) AppTheme.colors.textPrimary else Color.Transparent, androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (currentAccent == id) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "Selected",
                                            tint = AppTheme.colors.background,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(title, style = AppTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
                            }
                        }
                    }
                }
            }
        }

        if (showPaperSizeSheet) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showPaperSizeSheet = false },
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
                        Text("Select Paper Size", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
                        Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                        Text("Changing paper size adjusts receipt formatting automatically.", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(AppTheme.radius.md).liftClickable { scope.launch { prefRepo.setPaperSize(58); showPaperSizeSheet = false } }.padding(vertical = AppTheme.spacing.md, horizontal = AppTheme.spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = currentPaperSize == 58, 
                                onClick = { scope.launch { prefRepo.setPaperSize(58); showPaperSizeSheet = false } },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                    selectedColor = AppTheme.colors.accent,
                                    unselectedColor = AppTheme.colors.textSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("58 mm", style = AppTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppTheme.colors.divider))
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(AppTheme.radius.md).liftClickable { scope.launch { prefRepo.setPaperSize(80); showPaperSizeSheet = false } }.padding(vertical = AppTheme.spacing.md, horizontal = AppTheme.spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = currentPaperSize == 80, 
                                onClick = { scope.launch { prefRepo.setPaperSize(80); showPaperSizeSheet = false } },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                    selectedColor = AppTheme.colors.accent,
                                    unselectedColor = AppTheme.colors.textSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("80 mm", style = AppTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
                        }
                    }
                }
            }
        }

        
    }
}

@Composable
fun SettingsRow(
    title: String,
    icon: ImageVector,
    hasSwitch: Boolean = false,
    switchState: Boolean = false,
    onSwitchChange: ((Boolean) -> Unit)? = null,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .liftClickable(onClick = onClick)
            .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(AppTheme.dimensions.listItemIconSize)
                .clip(AppTheme.radius.md)
                .background(AppTheme.colors.surfaceLighter)
                .border(AppTheme.dimensions.borderThickness, Color.White.copy(alpha = 0.06f), AppTheme.radius.md),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppTheme.colors.textSecondary,
                modifier = Modifier.size(AppTheme.dimensions.iconSizeMd)
            )
        }
        
        Spacer(modifier = Modifier.width(AppTheme.spacing.md))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary
                )
            }
        }
        
        if (hasSwitch) {
            androidx.compose.material3.Switch(
                checked = switchState,
                onCheckedChange = onSwitchChange,
                colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AppTheme.colors.accent,
                    uncheckedThumbColor = AppTheme.colors.textSecondary,
                    uncheckedTrackColor = AppTheme.colors.surfaceLighter,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = AppTheme.colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
