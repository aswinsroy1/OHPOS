package com.example.ui.screens

import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.LocalAtm
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.rounded.LocalAtm
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import com.example.util.CurrencyFormatter
import com.example.ui.components.*
import com.example.ui.theme.AppTheme

import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    selectedTab: NavItem = NavItem.Home, 
    onTabSelected: (NavItem) -> Unit = {},
    onMenuClick: () -> Unit = {},
    onReportsLongClick: (() -> Unit)? = null,
    showReportsDot: Boolean = false,
    onProfileClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val totalOrders by viewModel.totalOrdersToday.collectAsState()
    val totalSales by viewModel.totalSalesToday.collectAsState()
    val recentOrders by viewModel.recentOrders.collectAsState()
    val topItems by viewModel.topItems.collectAsState()

    var currentlyOpenCardId by remember { mutableStateOf<Any?>(null) }
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var showTransactionHistory by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = showTransactionHistory) {
        showTransactionHistory = false
    }
    
    var selectedInvoiceId by remember { mutableStateOf<Int?>(null) }
    
    var selectedInvoiceRect by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val selectedInvoice = recentOrders.find { it.bill.id == selectedInvoiceId } 
    if (showTransactionHistory) {
        val reportsViewModel: ReportsViewModel = viewModel()
        TransactionHistoryScreen(
            onBackClick = { showTransactionHistory = false },
            viewModel = reportsViewModel
        )
        return
    }
    
    LaunchedEffect(showToast) {
        if (showToast) {
            kotlinx.coroutines.delay(3000)
            showToast = false
        }
    }

    var greetingText by remember { mutableStateOf("Good Morning") }
    val lifecycleOwner = LocalLifecycleOwner.current
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                greetingText = when (hour) {
                    in 5..11 -> "Good Morning"
                    in 12..16 -> "Good Afternoon"
                    in 17..20 -> "Good Evening"
                    else -> "Good Night"
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        // Initial setup
        val initialHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        greetingText = when (initialHour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    val dynamicTopPadding = 68.dp + (screenHeight * 0.02f)
    val spacerSmall = screenHeight * 0.01f
    val spacerMedium = screenHeight * 0.02f
    val spacerLarge = screenHeight * 0.03f

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
                    onMenuClick = onMenuClick,
                    onTrailingClick = onProfileClick,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = AppTheme.spacing.lg)
                )
            }
        },
        bottomBar = {
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppTheme.spacing.lg),
                contentPadding = PaddingValues(
                    top = dynamicTopPadding + WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = 110.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(spacerMedium)
            ) {
            // item {
            //    AppTopBar()
            // }
            item(key = "greeting_and_overview") {
                Spacer(modifier = Modifier.height(spacerSmall))
                
                Text(
                    text = greetingText,
                    style = AppTheme.typography.numberLarge,
                    color = AppTheme.colors.textPrimary
                )
                
                Spacer(modifier = Modifier.height(spacerSmall))
                
                Text(
                    text = "Here's today's overview",
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.textSecondary
                )
                
                Spacer(modifier = Modifier.height(spacerLarge))
            }
            item(key = "stats_cards") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                ) {
                    val averageBill = if (totalOrders > 0) totalSales / totalOrders else 0.0
                    


                    StatisticsCard(
                        title = "Total Orders",
                        value = totalOrders.toString(),
                        subtitle = "Today",
                        backTitle = "Average Bill",
                        backValue = com.example.util.CurrencyFormatter.formatNoDecimals(averageBill),
                        backSubtitle = "Today",
                        icon = Icons.Rounded.RestaurantMenu,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(NavItem.Reports) }
                    )

                    StatisticsCard(
                        title = "Total Sales",
                        value = com.example.util.CurrencyFormatter.formatNoDecimals(totalSales),
                        subtitle = "Today",
                        backTitle = "Customers",
                        backValue = totalOrders.toString(),
                        backSubtitle = "Today",
                        icon = Icons.Rounded.LocalAtm,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(NavItem.Reports) }
                    )
                }
            }
            item(key = "recent_orders_header") {
                Spacer(modifier = Modifier.height(spacerMedium))
                SectionHeader(
                    title = "Recent Orders",
                    actionText = "View all",
                    onActionClick = { showTransactionHistory = true }
                )
            }
            if (recentOrders.isEmpty()) {
                item {
                    EmptyState(
                        title = "No recent orders",
                        subtitle = "Your recent orders will appear here",
                        icon = Icons.Rounded.ShoppingCart
                    )
                }
            } else {
                items(count = recentOrders.size, key = { index -> recentOrders[index].bill.id }) { index ->
                    val billWithItems = recentOrders[index]
                    RecentOrderItem(
                        billWithItems = billWithItems,
                        selectedInvoiceId = selectedInvoiceId,
                        onClick = { rect -> 
                            selectedInvoiceId = billWithItems.bill.id
                            selectedInvoiceRect = rect
                        },
                        currentlyOpenCardId = currentlyOpenCardId,
                        onCardOpen = { currentlyOpenCardId = it },
                        onDelete = {
                            viewModel.requestDeletion(billWithItems.bill.id)
                            toastMessage = "Deletion requested"
                            showToast = true
                        },
                        onPrint = {
                            viewModel.printBill(context, billWithItems)
                        }
                    )
                }
            }
            item(key = "top_items_header") {
                Spacer(modifier = Modifier.height(spacerMedium))
                SectionHeader(
                    title = "Top Items",
                    actionText = "View all",
                    onActionClick = { onTabSelected(NavItem.Menu) }
                )
            }
            if (topItems.isEmpty()) {
                item {
                    EmptyState(
                        title = "No top items",
                        subtitle = "Your top selling items will appear here",
                        icon = Icons.Rounded.Cookie
                    )
                }
            } else {
                items(count = topItems.size, key = { index -> topItems[index].name }) { index ->
                    val item = topItems[index]
                    AppListItem(
                        title = item.name,
                        subtitle = "${item.totalQuantity} Orders",
                        trailingText = CurrencyFormatter.format(item.totalRevenue),
                        icon = Icons.Rounded.Cookie
                    )
                }
            }
        }
        
        // Toast
        androidx.compose.animation.AnimatedVisibility(
            visible = showToast,
            enter = androidx.compose.animation.fadeIn(animationSpec = com.example.ui.components.PremiumMotion.defaultSpring()) + androidx.compose.animation.slideInVertically(initialOffsetY = { -it }),
            exit = androidx.compose.animation.fadeOut(animationSpec = com.example.ui.components.PremiumMotion.defaultSpring()) + androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = dynamicTopPadding + 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(AppTheme.radius.full)
                    .background(AppTheme.colors.surface)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), AppTheme.radius.full)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(toastMessage, style = AppTheme.typography.labelMedium, color = AppTheme.colors.textPrimary)
            }
        }
        }
    }
    }
}

@Composable
fun RecentOrderItem(
    billWithItems: com.example.data.BillWithItems,
    selectedInvoiceId: Int? = null,
    onClick: (androidx.compose.ui.geometry.Rect) -> Unit = {},
    showBadge: Boolean = false,
    currentlyOpenCardId: Any? = null,
    onCardOpen: (Any) -> Unit = {},
    onDelete: (() -> Unit)? = null,
    onPrint: (() -> Unit)? = null
) {
    var itemRect by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val isSelected = billWithItems.bill.id == selectedInvoiceId
    val isPendingDeletion = billWithItems.bill.state == "PENDING_DELETION"

    com.example.ui.components.SwipeActionCard(
        cardId = billWithItems.bill.id,
        currentlyOpenCardId = currentlyOpenCardId,
        onCardOpen = onCardOpen,
        isSwipeEnabled = !isPendingDeletion && (onDelete != null || onPrint != null),
        onDelete = onDelete,
        onPrint = onPrint
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .onGloballyPositioned { itemRect = it.boundsInWindow() }
                .graphicsLayer { alpha = if (isSelected) 0f else 1f }
        ) {
            val date = java.util.Date(billWithItems.bill.timestamp)
            val formatter = java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault())
            
            com.example.ui.components.AppListItem(
                title = "Order #${billWithItems.bill.id}",
                subtitle = formatter.format(date),
                trailingText = com.example.util.CurrencyFormatter.formatNoDecimals(billWithItems.bill.totalAmount),
                icon = if (billWithItems.bill.orderMode == "Delivery") androidx.compose.material.icons.Icons.Rounded.ShoppingBag else androidx.compose.material.icons.Icons.Rounded.RestaurantMenu,
                statusText = if (showBadge) billWithItems.bill.orderMode else null,
                statusColor = if (showBadge) {
                    if (billWithItems.bill.orderMode == "Delivery") com.example.ui.theme.AppTheme.colors.textPrimary else com.example.ui.theme.AppTheme.colors.accent
                } else com.example.ui.theme.AppTheme.colors.textSecondary,
                onClick = { itemRect?.let { onClick(it) } },
                isPendingDeletion = isPendingDeletion
            )
        }
    }
}
