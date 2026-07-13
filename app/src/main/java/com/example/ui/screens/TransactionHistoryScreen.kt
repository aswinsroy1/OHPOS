package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.ui.components.*
import com.example.ui.theme.AppTheme

@Composable
fun TransactionHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: ReportsViewModel
) {
    val searchQuery by viewModel.historySearchQuery.collectAsState()
    val selectedTimeFilter by viewModel.historyTimeFilter.collectAsState()
    val historyBills by viewModel.filteredHistoryBills.collectAsState()

    val timeFilters = listOf("All Time", "Today", "Yesterday", "This Week", "This Month")

    var currentlyOpenCardId by remember { mutableStateOf<Any?>(null) }
    var selectedInvoiceId by remember { mutableStateOf<Int?>(null) }
    
    var selectedInvoiceRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val selectedInvoice = historyBills.find { it.bill.id == selectedInvoiceId } 
    val context = androidx.compose.ui.platform.LocalContext.current
    
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .bouncyClickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = AppTheme.colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                    Text(
                        text = "Transaction History",
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary
                    )
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppTheme.spacing.lg),
            contentPadding = PaddingValues(
                top = dynamicTopPadding + WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = 40.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            item {
                AppSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setHistorySearchQuery(it) },
                    placeholder = "Search by order number"
                )
            }

            item {
                CategoryChips(
                    categories = timeFilters,
                    selectedCategory = selectedTimeFilter,
                    onCategorySelected = { viewModel.setHistoryTimeFilter(it) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
            }

            if (historyBills.isEmpty()) {
                item {
                    EmptyState(
                        title = "No transactions found",
                        subtitle = "Try adjusting your filters",
                        icon = Icons.Rounded.BarChart
                    )
                }
            } else {
                items(
                    count = historyBills.size,
                    key = { historyBills[it].bill.id }
                ) { index ->
                    val billWithItems = historyBills[index]
                    RecentOrderItem(
                        billWithItems = billWithItems,
                        selectedInvoiceId = selectedInvoiceId,
                        onClick = { rect -> 
                            selectedInvoiceId = billWithItems.bill.id 
                            selectedInvoiceRect = rect
                        },
                        showBadge = true,
                        currentlyOpenCardId = currentlyOpenCardId,
                        onCardOpen = { currentlyOpenCardId = it },
                        onDelete = {
                            viewModel.requestDeletion(billWithItems.bill.id)
                        },
                        onPrint = {
                            viewModel.printBill(context, billWithItems)
                        }
                    )
                }
            }
        }
    }
}
}
