package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.example.data.AutoBackupPreferencesRepository
import com.example.ui.components.AppTopBar
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SectionHeader
import com.example.ui.components.liftClickable
import com.example.ui.theme.AppTheme
import com.example.util.AutoBackupScheduler
import com.example.util.AutomaticBackupManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoBackupSettingsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { AutoBackupPreferencesRepository(context) }

    val isAutoBackupEnabled by prefs.isAutoBackupEnabled.collectAsState(initial = false)
    val backupFolderUri by prefs.backupFolderUri.collectAsState(initial = null)
    val backupFrequencyHours by prefs.backupFrequencyHours.collectAsState(initial = 24)
    val backupRetention by prefs.backupRetention.collectAsState(initial = 10)
    val lastSuccessfulBackup by prefs.lastSuccessfulBackup.collectAsState(initial = 0L)
    val nextScheduledBackup by prefs.nextScheduledBackup.collectAsState(initial = 0L)
    
    var isRunningBackup by remember { mutableStateOf(false) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            // Persist access permissions
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, takeFlags)
            
            scope.launch {
                prefs.setBackupFolderUri(it.toString())
                Toast.makeText(context, "Backup folder selected", Toast.LENGTH_SHORT).show()
            }
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
                title = "Automatic Backups",
                onBackClick = onBackClick,
                modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = AppTheme.spacing.lg,
                    end = AppTheme.spacing.lg,
                    bottom = 120.dp
                )
            ) {
                // Master Switch
                item {
                    SectionHeader(title = "Master Switch")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(AppTheme.elevations.sm, AppTheme.radius.lg)
                            .clip(AppTheme.radius.lg)
                            .background(AppTheme.colors.surface)
                            .border(AppTheme.dimensions.borderThickness, Color.White.copy(alpha = 0.06f), AppTheme.radius.lg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enable Automatic Backups",
                                    style = AppTheme.typography.titleMedium,
                                    color = AppTheme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Automatically create backup files without user intervention.",
                                    style = AppTheme.typography.bodyMedium,
                                    color = AppTheme.colors.textSecondary
                                )
                            }
                            Switch(
                                checked = isAutoBackupEnabled,
                                onCheckedChange = { checked ->
                                    scope.launch {
                                        prefs.setAutoBackupEnabled(checked)
                                        if (checked) {
                                            AutoBackupScheduler.schedule(context, backupFrequencyHours)
                                        } else {
                                            AutoBackupScheduler.cancel(context)
                                        }
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AppTheme.colors.accent,
                                    uncheckedThumbColor = AppTheme.colors.textSecondary,
                                    uncheckedTrackColor = AppTheme.colors.surfaceLighter,
                                    uncheckedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }
                }

                // Backup Location
                item {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    SectionHeader(title = "Backup Location")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(AppTheme.elevations.sm, AppTheme.radius.lg)
                            .clip(AppTheme.radius.lg)
                            .background(AppTheme.colors.surface)
                            .border(AppTheme.dimensions.borderThickness, Color.White.copy(alpha = 0.06f), AppTheme.radius.lg)
                            .liftClickable { folderPickerLauncher.launch(null) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
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
                                    imageVector = Icons.Rounded.Folder,
                                    contentDescription = null,
                                    tint = AppTheme.colors.textSecondary,
                                    modifier = Modifier.size(AppTheme.dimensions.iconSizeMd)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Backup Folder",
                                    style = AppTheme.typography.bodyLarge,
                                    color = AppTheme.colors.textPrimary
                                )
                                
                                val displayPath = if (backupFolderUri != null) {
                                    try {
                                        val uri = Uri.parse(backupFolderUri)
                                        val path = uri.path ?: ""
                                        val pathAfterColon = path.substringAfterLast(":")
                                        val parts = pathAfterColon.split("/")
                                        if (parts.size > 2) {
                                            "${parts.first()}/.../${parts.last()}"
                                        } else {
                                            pathAfterColon
                                        }
                                    } catch (e: Exception) {
                                        "Selected folder"
                                    }
                                } else "No backup folder selected."
                                
                                Text(
                                    text = displayPath,
                                    style = AppTheme.typography.bodyMedium,
                                    color = AppTheme.colors.textSecondary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
                            
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = AppTheme.colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Backups will be saved to the selected folder.",
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textSecondary,
                        modifier = Modifier.padding(horizontal = AppTheme.spacing.md)
                    )
                }

                // Backup Frequency
                item {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    SectionHeader(title = "Backup Frequency")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(AppTheme.elevations.sm, AppTheme.radius.lg)
                            .clip(AppTheme.radius.lg)
                            .background(AppTheme.colors.surface)
                            .border(AppTheme.dimensions.borderThickness, Color.White.copy(alpha = 0.06f), AppTheme.radius.lg)
                    ) {
                        Column {
                            val options = listOf(
                                6 to "Every 6 hours",
                                12 to "Every 12 hours",
                                24 to "Every 24 hours (Default)",
                                72 to "Every 3 days",
                                168 to "Weekly",
                                720 to "Monthly"
                            )
                            options.forEachIndexed { index, (hours, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .liftClickable {
                                            scope.launch {
                                                prefs.setBackupFrequencyHours(hours)
                                                if (isAutoBackupEnabled) {
                                                    AutoBackupScheduler.schedule(context, hours)
                                                }
                                            }
                                        }
                                        .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = backupFrequencyHours == hours,
                                        onClick = {
                                            scope.launch {
                                                prefs.setBackupFrequencyHours(hours)
                                                if (isAutoBackupEnabled) {
                                                    AutoBackupScheduler.schedule(context, hours)
                                                }
                                            }
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = AppTheme.colors.accent,
                                            unselectedColor = AppTheme.colors.textSecondary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
                                    Text(
                                        text = label,
                                        style = AppTheme.typography.bodyLarge,
                                        color = AppTheme.colors.textPrimary
                                    )
                                }
                                if (index < options.size - 1) {
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

                // Backup Retention
                item {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    SectionHeader(title = "Backup Retention")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(AppTheme.elevations.sm, AppTheme.radius.lg)
                            .clip(AppTheme.radius.lg)
                            .background(AppTheme.colors.surface)
                            .border(AppTheme.dimensions.borderThickness, Color.White.copy(alpha = 0.06f), AppTheme.radius.lg)
                    ) {
                        Column {
                            val options = listOf(
                                5 to "5",
                                10 to "10 (Default)",
                                20 to "20",
                                -1 to "Unlimited"
                            )
                            options.forEachIndexed { index, (amount, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .liftClickable {
                                            scope.launch { prefs.setBackupRetention(amount) }
                                        }
                                        .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = backupRetention == amount,
                                        onClick = { scope.launch { prefs.setBackupRetention(amount) } },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = AppTheme.colors.accent,
                                            unselectedColor = AppTheme.colors.textSecondary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
                                    Text(
                                        text = label,
                                        style = AppTheme.typography.bodyLarge,
                                        color = AppTheme.colors.textPrimary
                                    )
                                }
                                if (index < options.size - 1) {
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

                // Backup Naming
                item {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    SectionHeader(title = "Backup Naming")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(AppTheme.elevations.sm, AppTheme.radius.lg)
                            .clip(AppTheme.radius.lg)
                            .background(AppTheme.colors.surface)
                            .border(AppTheme.dimensions.borderThickness, Color.White.copy(alpha = 0.06f), AppTheme.radius.lg)
                            .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.md)
                    ) {
                        Text(
                            text = "OHPOS_Backup_2026-07-04_20-30.json",
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.textSecondary
                        )
                    }
                }

                // Backup Status
                item {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    SectionHeader(title = "Backup Status")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(AppTheme.elevations.sm, AppTheme.radius.lg)
                            .clip(AppTheme.radius.lg)
                            .background(AppTheme.colors.surface)
                            .border(AppTheme.dimensions.borderThickness, Color.White.copy(alpha = 0.06f), AppTheme.radius.lg)
                            .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.md)
                    ) {
                        Column {
                            if (!isAutoBackupEnabled) {
                                Text(
                                    text = "Automatic backups are disabled.",
                                    style = AppTheme.typography.bodyLarge,
                                    color = AppTheme.colors.textSecondary
                                )
                            } else {
                                val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
                                val lastBackupStr = if (lastSuccessfulBackup > 0) dateFormat.format(Date(lastSuccessfulBackup)) else "Never"
                                // Estimate next backup purely for UI purposes
                                val nextBackupStr = if (lastSuccessfulBackup > 0) {
                                    val nextTime = lastSuccessfulBackup + (backupFrequencyHours * 3600000L)
                                    dateFormat.format(Date(nextTime))
                                } else "Pending"

                                var fileCount = 0
                                if (backupFolderUri != null) {
                                    try {
                                        val folder = DocumentFile.fromTreeUri(context, Uri.parse(backupFolderUri!!))
                                        val count = folder?.listFiles()?.count { 
                                            val name = it.name ?: ""
                                            name.startsWith("OH_POS_Backup_") && name.endsWith(".zip") 
                                        }
                                        if (count != null) {
                                            fileCount = count
                                        }
                                    } catch (e: Exception) {}
                                }

                                Text("Last successful backup: $lastBackupStr", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Next scheduled backup: $nextBackupStr", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Number of automatic backups stored: $fileCount", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                            }
                        }
                    }
                }

                // Run Backup Now
                item {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    PrimaryButton(
                        text = if (isRunningBackup) "Running..." else "Run Backup Now",
                        onClick = {
                            if (!isRunningBackup) {
                                isRunningBackup = true
                                scope.launch {
                                    val success = AutomaticBackupManager.performAutomaticBackup(context)
                                    isRunningBackup = false
                                    Toast.makeText(
                                        context, 
                                        if (success) "Backup created successfully!" else "Backup failed. Please check folder permissions.", 
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
