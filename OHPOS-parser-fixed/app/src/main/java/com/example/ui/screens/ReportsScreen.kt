package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalAtm
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.theme.AppTheme
import com.example.util.CurrencyFormatter

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun ReportsScreen(
    selectedTab: NavItem,
    onTabSelected: (NavItem) -> Unit,
    onMenuClick: () -> Unit = {},
    onReportsLongClick: (() -> Unit)? = null,
    showReportsDot: Boolean = false,
    onDayClosingClick: () -> Unit = {},
    viewModel: ReportsViewModel = viewModel()
) {
    val selectedTimeFilter by viewModel.selectedTimeFilter.collectAsState()
    val totalSales by viewModel.totalSales.collectAsState()
    val totalOrders by viewModel.totalOrders.collectAsState()
    val topCategories by viewModel.topCategories.collectAsState()
    val salesChartData by viewModel.salesChartData.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val archivedReports by viewModel.archivedReports.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedReport by remember { mutableStateOf<com.example.data.DailyClosure?>(null) }
    
    var showTransactionHistory by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = showTransactionHistory) {
        showTransactionHistory = false
    }
    
    var selectedInvoiceId by remember { mutableStateOf<Int?>(null) }
    
    var selectedInvoiceRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val selectedInvoice = recentTransactions.find { it.bill.id == selectedInvoiceId } 

    if (showTransactionHistory) {
        TransactionHistoryScreen(
            onBackClick = { showTransactionHistory = false },
            viewModel = viewModel
        )
        return
    }

    val timeFilters = listOf("Today", "This Week", "This Month")
    val selectedFilterIndex = timeFilters.indexOf(selectedTimeFilter).takeIf { it >= 0 } ?: 0

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val dynamicTopPadding = 68.dp + (screenHeight * 0.02f)

    InvoicePreviewOverlay(
        isVisible = selectedInvoiceId != null,
        billWithItems = selectedInvoice,
        sourceRect = selectedInvoiceRect,
        onDismissRequest = { selectedInvoiceId = null },
        
    ) {
        AppScaffold(
            topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.background)
            ) {
                AppTopBar(
                    title = "Reports",
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
                .padding(horizontal = AppTheme.spacing.lg),
            contentPadding = PaddingValues(
                top = dynamicTopPadding + WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = 110.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
        ) {
            item {
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                SegmentedControl(
                    items = timeFilters,
                    selectedIndex = selectedFilterIndex,
                    onItemSelected = { index ->
                        viewModel.setTimeFilter(timeFilters[index])
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                ) {


                    StatisticsCard(
                        title = "Total Sales",
                        value = CurrencyFormatter.formatNoDecimals(totalSales),
                        subtitle = selectedTimeFilter,
                        icon = Icons.Rounded.LocalAtm,
                        modifier = Modifier.weight(1f)
                    )

                    StatisticsCard(
                        title = "Total Orders",
                        value = totalOrders.toString(),
                        subtitle = selectedTimeFilter,
                        icon = Icons.Rounded.RestaurantMenu,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                SectionHeader(
                    title = "Recent Transactions",
                    actionText = "View all",
                    onActionClick = { showTransactionHistory = true }
                )
            }

            if (recentTransactions.isEmpty()) {
                item {
                    EmptyState(
                        title = "No recent transactions",
                        subtitle = "Your completed bills will appear here",
                        icon = Icons.Rounded.BarChart
                    )
                }
            } else {
                items(count = recentTransactions.size, key = { index -> recentTransactions[index].bill.id }) { index ->
                    val billWithItems = recentTransactions[index]
                    RecentOrderItem(
                        billWithItems = billWithItems,
                        selectedInvoiceId = selectedInvoiceId,
                        onClick = { rect -> 
                            selectedInvoiceId = billWithItems.bill.id 
                            selectedInvoiceRect = rect
                        },
                        showBadge = true
                    )
                }
            }

            if (totalOrders == 0) {
                item {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    EmptyState(
                        icon = Icons.Rounded.BarChart,
                        title = "No sales yet",
                        subtitle = "Complete your first bill to see reports."
                    )
                }
            } else {
                item {
                    SectionHeader(title = "Sales Overview")
                }
                
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(AppTheme.radius.lg)
                            .background(AppTheme.colors.surface)
                            .padding(AppTheme.spacing.md)
                    ) {
                        BarChart(
                            data = salesChartData.map { 
                                BarChartData(it.label, it.sales, it.orders) 
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                    SectionHeader(title = "Top Categories")
                }
                
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val colors = listOf(
                            AppTheme.colors.accent,
                            Color(0xFF81C784),
                            Color(0xFF64B5F6),
                            Color(0xFFFFB74D),
                            Color(0xFFE57373)
                        )
                        DonutChart(
                            percentages = topCategories.map { it.percentage },
                            colors = colors,
                            modifier = Modifier.size(160.dp)
                        )
                    }
                }
                
                items(count = topCategories.size, key = { index -> topCategories[index].name }) { index ->
                    val stat = topCategories[index]
                    val colors = listOf(
                        AppTheme.colors.accent,
                        Color(0xFF81C784),
                        Color(0xFF64B5F6),
                        Color(0xFFFFB74D),
                        Color(0xFFE57373)
                    )
                    val dotColor = colors.getOrElse(index) { colors.last() }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppTheme.radius.md)
                            .background(AppTheme.colors.surface)
                            .padding(AppTheme.spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(dotColor)
                            )
                            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
                            Text(
                                text = stat.name,
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
                            Text(
                                text = "${(stat.percentage * 100).toInt()}%",
                                style = AppTheme.typography.labelMedium,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                        Text(
                            text = CurrencyFormatter.formatNoDecimals(stat.revenue),
                            style = AppTheme.typography.titleMedium,
                            color = AppTheme.colors.textPrimary
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    SectionHeader(title = "Archived Daily Reports")
                }
                
                if (archivedReports.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No archived reports",
                            subtitle = "Close a business day to generate a report",
                            icon = Icons.Rounded.EventAvailable
                        )
                    }
                } else {
                    items(count = archivedReports.size, key = { index -> archivedReports[index].id }) { index ->
                        val report = archivedReports[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(AppTheme.radius.md)
                                .background(AppTheme.colors.surface)
                                .bouncyClickable {
                                    selectedReport = report
                                }
                                .padding(AppTheme.spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = report.dateString,
                                    style = AppTheme.typography.titleMedium,
                                    color = AppTheme.colors.textPrimary
                                )
                                Text(
                                    text = "${report.totalOrders} Orders • ${CurrencyFormatter.formatNoDecimals(report.totalSales)}",
                                    style = AppTheme.typography.labelMedium,
                                    color = AppTheme.colors.textSecondary
                                )
                            }
                            if (report.pdfFilePath != null) {
                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = "View PDF",
                                    tint = AppTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppTheme.radius.md)
                            .background(AppTheme.colors.surface)
                            .bouncyClickable { onDayClosingClick() }
                            .padding(AppTheme.spacing.lg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(AppTheme.colors.accent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.EventAvailable,
                                    contentDescription = null,
                                    tint = AppTheme.colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                            Column {
                                Text(
                                    text = "Day Closing",
                                    style = AppTheme.typography.titleMedium,
                                    color = AppTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Close business day & export summary",
                                    style = AppTheme.typography.labelMedium,
                                    color = AppTheme.colors.textSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = AppTheme.colors.textSecondary
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xxl))
                }
            }
        }

    if (selectedReport != null) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { selectedReport = null },
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
                    text = "Report: ${selectedReport?.dateString}",
                    style = AppTheme.typography.titleLarge,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.padding(bottom = AppTheme.spacing.md)
                )
                
                val items = listOf(
                    "View PDF" to { 
                        if (selectedReport?.pdfFilePath != null) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                intent.setDataAndType(android.net.Uri.parse(selectedReport!!.pdfFilePath), "application/pdf")
                                intent.flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                context.startActivity(intent)
                            } catch(e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    "Share PDF" to {
                        if (selectedReport?.pdfFilePath != null) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND)
                                intent.type = "application/pdf"
                                intent.putExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri.parse(selectedReport!!.pdfFilePath))
                                intent.flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                context.startActivity(android.content.Intent.createChooser(intent, "Share Report"))
                            } catch(e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    "Cancel" to { selectedReport = null }
                )
                
                items.forEach { (title, onClick) ->
                    Text(
                        text = title,
                        style = AppTheme.typography.bodyLarge,
                        color = if (title == "Cancel") AppTheme.colors.textSecondary else AppTheme.colors.textPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppTheme.radius.md)
                            .bouncyClickable {
                                onClick()
                                if (title != "Cancel") selectedReport = null
                            }
                            .padding(AppTheme.spacing.md)
                    )
                }
            }
        }
    }
}
}
}
