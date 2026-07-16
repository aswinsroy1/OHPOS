package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppScaffold
import com.example.ui.components.AppTopBar
import androidx.compose.material3.Button
import com.example.ui.components.bouncyClickable
import com.example.ui.theme.AppTheme
import com.example.util.CurrencyFormatter

@Composable
fun DayClosingScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DayClosingViewModel = viewModel()
    val stats by viewModel.dayStats.collectAsState()
    val isDayClosed by viewModel.isDayClosed.collectAsState()
    val lastClosure by viewModel.lastClosure.collectAsState()
    
    val dailyClosingPrefRepo = remember { com.example.data.DailyClosingPreferencesRepository(context) }
    val savedFolderUri by dailyClosingPrefRepo.exportFolderUri.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showLoadingDialog by remember { mutableStateOf(false) }
    
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            scope.launch {
                dailyClosingPrefRepo.setExportFolderUri(uri.toString())
                showConfirmDialog = true
            }
        }
    }
    
    val handleCloseDay = {
        if (savedFolderUri == null) {
            folderPicker.launch(null)
        } else {
            showConfirmDialog = true
        }
    }
    
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Close Business Day?", style = AppTheme.typography.titleLarge) },
            text = { Text("This will archive today's business records and generate a Daily Closing Report. This action cannot be undone.", style = AppTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        showLoadingDialog = true
                        viewModel.closeDay(
                            context = context,
                            folderUri = Uri.parse(savedFolderUri!!),
                            onSuccess = {
                                showLoadingDialog = false
                                onBackClick()
                            },
                            onError = { reason ->
                                showLoadingDialog = false
                                Toast.makeText(context, "Error: $reason", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.colors.accent)
                ) {
                    Text("Close Day")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.colors.textSecondary)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = AppTheme.colors.surface,
            titleContentColor = AppTheme.colors.textPrimary,
            textContentColor = AppTheme.colors.textSecondary
        )
    }
    
    if (showLoadingDialog) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                modifier = Modifier
                    .clip(AppTheme.radius.lg)
                    .background(AppTheme.colors.surface)
                    .padding(AppTheme.spacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AppTheme.colors.accent)
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                    Text("Generating Daily Report...", color = AppTheme.colors.textPrimary)
                }
            }
        }
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = "Day Closing",
                onBackClick = onBackClick,
                onTrailingClick = {} // Keep it empty for now
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppTheme.spacing.lg),
            contentPadding = PaddingValues(
                top = 100.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            item {
                Text(
                    text = "Live Day Summary",
                    style = AppTheme.typography.titleLarge,
                    color = AppTheme.colors.textPrimary
                )
            }
            
            val summaryItems = listOf(
                "Restaurant Name" to stats.restaurantName,
                "Business Date" to stats.businessDate,
                "Orders" to stats.orders.toString(),
                "Gross Sales" to CurrencyFormatter.format(stats.grossSales),
                "GST Collected" to CurrencyFormatter.format(stats.gstCollected),
                "Discounts" to CurrencyFormatter.format(stats.discounts),
                "Cash" to CurrencyFormatter.format(stats.cash),
                "UPI" to CurrencyFormatter.format(stats.upi),
                "Card" to CurrencyFormatter.format(stats.card),
                "Average Bill" to CurrencyFormatter.format(stats.averageBill),
                "Highest Bill" to CurrencyFormatter.format(stats.highestBill),
                "Lowest Bill" to CurrencyFormatter.format(stats.lowestBill),
                "Top Selling Item" to stats.topSellingItem,
                "Top Selling Category" to stats.topSellingCategory,
                "Items Sold" to stats.itemsSold.toString(),
                "Opening Time" to stats.openingTime,
                "Current Time" to stats.currentTime
            )
            
            for ((label, value) in summaryItems) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppTheme.radius.md)
                            .background(AppTheme.colors.surface)
                            .padding(AppTheme.spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                        Text(value, style = AppTheme.typography.titleMedium, color = AppTheme.colors.textPrimary)
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                if (isDayClosed) {
                    val closureTimeStr = lastClosure?.let { 
                        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(it.closedAtTimestamp))
                    } ?: "unknown time"
                    
                    Text(
                        text = "Today's business day was already closed at $closureTimeStr.",
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colors.accent,
                        modifier = Modifier.fillMaxWidth().padding(vertical = AppTheme.spacing.sm),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    Button(
                        onClick = handleCloseDay,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accent),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.EventAvailable, contentDescription = null, tint = AppTheme.colors.background)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Close Business Day", style = AppTheme.typography.titleMedium, color = AppTheme.colors.background)
                    }
                }
            }
        }
    }
}
