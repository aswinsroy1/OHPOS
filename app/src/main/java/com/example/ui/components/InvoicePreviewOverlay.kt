package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.core.content.FileProvider
import com.example.data.BillWithItems
import com.example.ui.theme.AppTheme
import com.example.util.CurrencyFormatter
import com.example.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoicePreviewOverlay(
    isVisible: Boolean,
    billWithItems: BillWithItems?,
    sourceRect: androidx.compose.ui.geometry.Rect?,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onFullyClosed: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }

    var lastBill by remember { mutableStateOf(billWithItems) }
    if (billWithItems != null) {
        lastBill = billWithItems
    }
    
    var lastSourceRect by remember { mutableStateOf(sourceRect) }
    if (sourceRect != null && isVisible) {
        lastSourceRect = sourceRect
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null && lastBill != null) {
            coroutineScope.launch {
                isExporting = true
                withContext(Dispatchers.IO) {
                    val tempPdf = PdfGenerator.generateInvoicePdf(context, lastBill!!)
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        FileInputStream(tempPdf).use { input ->
                            input.copyTo(output)
                        }
                    }
                    tempPdf.delete()
                }
                isExporting = false
            }
        }
    }

    PremiumModalOverlay(
        isVisible = isVisible,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        },
        overlayContent = {
            val transition = updateTransition(targetState = isVisible, label = "invoice_transition")
            val showContent = transition.currentState || transition.targetState
            
            LaunchedEffect(transition.currentState, isVisible) {
                if (!transition.currentState && !isVisible) {
                    onFullyClosed()
                }
            }
            
            if (showContent && lastBill != null) {
                val density = androidx.compose.ui.platform.LocalDensity.current
                val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                val screenHeight = LocalConfiguration.current.screenHeightDp.dp
                val topPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
                                val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                val verticalMargin = 32.dp
                val safeScreenHeight = screenHeight - topPadding - bottomPadding
                val maxAvailableHeight = (safeScreenHeight - (verticalMargin * 2)).coerceAtLeast(100.dp)
                
                // Fixed preferred height that doesn't depend on item count
                val preferredHeight = 650.dp
                val expandedHeight = minOf(preferredHeight, maxAvailableHeight)
                
                // Vertically center the dialog in the safe area
                val expandedTop = topPadding + verticalMargin + ((maxAvailableHeight - expandedHeight) / 2)
                
                val expandedWidth = screenWidth - 32.dp
                val expandedLeft = 16.dp




                val sourceTop = with(density) { lastSourceRect?.top?.toDp() ?: expandedTop }
                val sourceLeft = with(density) { lastSourceRect?.left?.toDp() ?: expandedLeft }
                val sourceWidth = with(density) { lastSourceRect?.width?.toDp() ?: expandedWidth }
                val sourceHeight = with(density) { lastSourceRect?.height?.toDp() ?: expandedHeight }
                
                val top by transition.animateDp(
                    transitionSpec = { PremiumMotion.defaultSpring() },
                    label = "top"
                ) { visible -> if (visible) expandedTop else sourceTop }
                
                val left by transition.animateDp(
                    transitionSpec = { PremiumMotion.defaultSpring() },
                    label = "left"
                ) { visible -> if (visible) expandedLeft else sourceLeft }
                
                val width by transition.animateDp(
                    transitionSpec = { PremiumMotion.defaultSpring() },
                    label = "width"
                ) { visible -> if (visible) expandedWidth else sourceWidth }
                
                val height by transition.animateDp(
                    transitionSpec = { PremiumMotion.defaultSpring() },
                    label = "height"
                ) { visible -> if (visible) expandedHeight else sourceHeight }

                val expandFraction by transition.animateFloat(
                    transitionSpec = { PremiumMotion.defaultSpring() },
                    label = "expandFraction"
                ) { visible -> if (visible) 1f else 0f }

                Box(
                    modifier = Modifier
                        .offset(x = left, y = top)
                        .size(width = width, height = height)
                        .clip(AppTheme.radius.lg)
                        .background(AppTheme.colors.surface)
                        .border(AppTheme.dimensions.borderThickness, AppTheme.colors.borderLight, AppTheme.radius.lg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {}
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = 1f - expandFraction }
                    ) {
                        val date = Date(lastBill!!.bill.timestamp)
                        val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                        
                        AppListItem(
                            title = "Order #${lastBill!!.bill.id}",
                            subtitle = formatter.format(date),
                            trailingText = com.example.util.CurrencyFormatter.formatNoDecimals(lastBill!!.bill.totalAmount),
                            icon = if (lastBill!!.bill.orderMode == "Delivery") androidx.compose.material.icons.Icons.Rounded.ShoppingBag else androidx.compose.material.icons.Icons.Rounded.RestaurantMenu,
                            statusText = lastBill!!.bill.orderMode,
                            statusColor = if (lastBill!!.bill.orderMode == "Delivery") AppTheme.colors.textPrimary else AppTheme.colors.accent,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .requiredSize(width = expandedWidth, height = expandedHeight)
                            .padding(AppTheme.spacing.lg)
                            .graphicsLayer { alpha = expandFraction }
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(Unit) {
                                        detectVerticalDragGestures { _, dragAmount ->
                                            if (dragAmount > 20f) {
                                                onDismissRequest()
                                            }
                                        }
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(AppTheme.radius.md)
                                            .background(AppTheme.colors.accent.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ReceiptLong,
                                            contentDescription = null,
                                            tint = AppTheme.colors.accent
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                                    Column {
                                        Text(
                                            text = "Order #${lastBill!!.bill.id}",
                                            style = AppTheme.typography.titleMedium,
                                            color = AppTheme.colors.textPrimary
                                        )
                                        val date = Date(lastBill!!.bill.timestamp)
                                        val formatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                                        Text(
                                            text = formatter.format(date),
                                            style = AppTheme.typography.bodyMedium,
                                            color = AppTheme.colors.textSecondary
                                        )
                                    }
                                }
                                IconButton(onClick = onDismissRequest) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Close",
                                        tint = AppTheme.colors.textSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Order Type", style = AppTheme.typography.labelMedium, color = AppTheme.colors.textSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(AppTheme.radius.full)
                                            .background(
                                                if (lastBill!!.bill.orderMode == "Delivery") AppTheme.colors.textPrimary.copy(alpha = 0.1f)
                                                else AppTheme.colors.accent.copy(alpha = 0.1f)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = lastBill!!.bill.orderMode,
                                            style = AppTheme.typography.labelMedium,
                                            color = if (lastBill!!.bill.orderMode == "Delivery") AppTheme.colors.textPrimary else AppTheme.colors.accent
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Status", style = AppTheme.typography.labelMedium, color = AppTheme.colors.textSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(AppTheme.radius.full)
                                            .background(Color(0xFF81C784).copy(alpha = 0.1f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = lastBill!!.bill.printStatus,
                                            style = AppTheme.typography.labelMedium,
                                            color = Color(0xFF81C784)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(AppTheme.colors.borderLight)
                            )
                            Spacer(modifier = Modifier.height(AppTheme.spacing.md))

                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = AppTheme.spacing.sm),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Item", style = AppTheme.typography.labelMedium, color = AppTheme.colors.textSecondary, modifier = Modifier.weight(2f))
                                Text(text = "Qty", style = AppTheme.typography.labelMedium, color = AppTheme.colors.textSecondary, modifier = Modifier.weight(0.5f))
                                Text(text = "Price", style = AppTheme.typography.labelMedium, color = AppTheme.colors.textSecondary, modifier = Modifier.weight(1f))
                                Text(text = "Total", style = AppTheme.typography.labelMedium, color = AppTheme.colors.textSecondary, modifier = Modifier.weight(1f))
                            }

                            // Items
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(lastBill!!.items) { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = AppTheme.spacing.sm),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.menuItemName,
                                            style = AppTheme.typography.bodyMedium,
                                            color = AppTheme.colors.textPrimary,
                                            modifier = Modifier.weight(2f)
                                        )
                                        Text(
                                            text = "${item.quantity}",
                                            style = AppTheme.typography.bodyMedium,
                                            color = AppTheme.colors.textSecondary,
                                            modifier = Modifier.weight(0.5f)
                                        )
                                        Text(
                                            text = CurrencyFormatter.formatNoDecimals(item.price),
                                            style = AppTheme.typography.bodyMedium,
                                            color = AppTheme.colors.textSecondary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = CurrencyFormatter.formatNoDecimals(item.price * item.quantity),
                                            style = AppTheme.typography.bodyMedium,
                                            color = AppTheme.colors.textPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(AppTheme.colors.borderLight)
                            )
                            Spacer(modifier = Modifier.height(AppTheme.spacing.md))

                            // Summary
                            val subtotal = lastBill!!.items.sumOf { it.price * it.quantity }
                            val gst = lastBill!!.bill.totalAmount - subtotal

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Subtotal", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                                Text(text = CurrencyFormatter.formatNoDecimals(subtotal), style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textPrimary)
                            }
                            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "GST (${lastBill!!.bill.gstRatePercent}%)", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                                Text(text = CurrencyFormatter.formatNoDecimals(gst), style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textPrimary)
                            }
                            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Grand Total", style = AppTheme.typography.titleMedium, color = AppTheme.colors.textPrimary)
                                Text(text = CurrencyFormatter.formatNoDecimals(lastBill!!.bill.totalAmount), style = AppTheme.typography.titleLarge, color = AppTheme.colors.accent)
                            }

                            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

                            // Action Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(AppTheme.radius.md)
                                        .background(AppTheme.colors.accent.copy(alpha = 0.1f))
                                        .clickable {
                                            createDocumentLauncher.launch("Invoice_${lastBill!!.bill.id}.pdf")
                                        }
                                        .padding(AppTheme.spacing.md),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Download,
                                            contentDescription = "Save PDF",
                                            tint = AppTheme.colors.accent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Save PDF",
                                            style = AppTheme.typography.labelMedium,
                                            color = AppTheme.colors.accent
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(AppTheme.radius.md)
                                        .background(AppTheme.colors.accent)
                                        .clickable {
                                            coroutineScope.launch {
                                                isExporting = true
                                                withContext(Dispatchers.IO) {
                                                    val tempPdf = PdfGenerator.generateInvoicePdf(context, lastBill!!)
                                                    val uri = FileProvider.getUriForFile(
                                                        context,
                                                        "${context.packageName}.fileprovider",
                                                        tempPdf
                                                    )
                                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "application/pdf"
                                                        putExtra(Intent.EXTRA_STREAM, uri)
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(Intent.createChooser(shareIntent, "Share Invoice"))
                                                }
                                                isExporting = false
                                            }
                                        }
                                        .padding(AppTheme.spacing.md),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Share,
                                            contentDescription = "Share PDF",
                                            tint = AppTheme.colors.background,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Share PDF",
                                            style = AppTheme.typography.labelMedium,
                                            color = AppTheme.colors.background
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
