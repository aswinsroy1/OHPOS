package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.rounded.Close
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.PrintDisabled
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.os.Build
import com.example.ui.components.*
import com.example.ui.theme.AppTheme
import com.example.data.MenuItem

@Composable
fun BillingScreen(
    selectedTab: NavItem = NavItem.Billing,
    onTabSelected: (NavItem) -> Unit = {},
    onMenuClick: () -> Unit = {},
    onReportsLongClick: (() -> Unit)? = null,
    showReportsDot: Boolean = false,
    onOpenSettings: () -> Unit = {},
    viewModel: BillingViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val menuItems by viewModel.activeMenuItems.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val printEvent by viewModel.printEvent.collectAsState()
    val defaultPrinter by viewModel.defaultPrinter.collectAsState()
    val printerStatuses by com.example.util.PrinterStatusMonitor.statuses.collectAsState()
    val defaultPrinterStatus = defaultPrinter?.id?.let { printerStatuses[it] }?.state ?: com.example.util.PrinterState.DISCONNECTED
    
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val gstPrefRepo = remember { com.example.data.GstPreferencesRepository(context) }
    val restaurantGst by gstPrefRepo.restaurantGstPercent.collectAsState(initial = 5.0)
    val deliveryGst by gstPrefRepo.deliveryGstPercent.collectAsState(initial = 18.0)
    
    LaunchedEffect(showToast) {
        if (showToast) {
            kotlinx.coroutines.delay(3000)
            showToast = false
        }
    }

    var selectedOrderType by remember { mutableStateOf(0) } // 0: Restaurant, 1: Delivery
    val currentGstRate = if (selectedOrderType == 1) deliveryGst else restaurantGst
    
    var isCartExpanded by remember { mutableStateOf(false) }

    BackHandler(enabled = isCartExpanded) {
        isCartExpanded = false
    }
    var showClearCartDialog by remember { mutableStateOf(false) }
    var isGstEnabled by remember { mutableStateOf(true) }

    val categories by viewModel.categories.collectAsState()
    
    val totalItems = remember(cart) { cart.sumOf { it.quantity } }

    LaunchedEffect(totalItems) {
        if (totalItems == 0) {
            isCartExpanded = false
        }
    }

    val subtotal = remember(cart, selectedOrderType) { 
        cart.sumOf { 
            val effectivePrice = if (selectedOrderType == 1 && it.menuItem.deliveryPrice != null) it.menuItem.deliveryPrice else it.menuItem.price
            effectivePrice * it.quantity 
        } 
    }

    val animatedGst by animateFloatAsState(
        targetValue = if (isGstEnabled) (subtotal * (currentGstRate / 100.0)).toFloat() else 0f,
        animationSpec = spring(dampingRatio = 0.76f, stiffness = 180f),
        label = "gst"
    )
    val animatedTotal = subtotal.toFloat() + animatedGst

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    val dynamicTopPadding = 68.dp + (screenHeight * 0.02f)
    val spacerSmall = screenHeight * 0.01f
    val spacerMedium = screenHeight * 0.02f
    val spacerLarge = screenHeight * 0.03f

    Box(modifier = Modifier.fillMaxSize()) {
        PremiumModalOverlay(
        isVisible = isCartExpanded,
        onDismissRequest = { isCartExpanded = false },
        content = {
            AppScaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.background)
                ) {
                    AppTopBar(
                        title = "New Bill",
                        subtitle = {
                            if (defaultPrinter != null) {
                                val statusColor = when (defaultPrinterStatus) {
                                    com.example.util.PrinterState.CONNECTED -> Color(0xFF4CAF50)
                                    com.example.util.PrinterState.CONNECTING -> Color(0xFFFFC107)
                                    com.example.util.PrinterState.STANDBY -> Color(0xFF9E9E9E)
                                    com.example.util.PrinterState.OFFLINE -> Color(0xFFE53935)
                                    com.example.util.PrinterState.DISCONNECTED -> Color(0xFF424242)
                                }
                                val statusText = when (defaultPrinterStatus) {
                                    com.example.util.PrinterState.CONNECTED -> "Printer Ready"
                                    com.example.util.PrinterState.OFFLINE, com.example.util.PrinterState.DISCONNECTED -> "Printer Offline"
                                    else -> "Connecting..."
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = statusText,
                                        style = AppTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                        color = statusColor
                                    )
                                }
                            }
                        },
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
            val filteredMenuItems = remember(menuItems, selectedOrderType) {
                if (selectedOrderType == 1) {
                    menuItems.filter { it.deliveryPrice != null }
                } else {
                    menuItems
                }
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 0.dp), // Padding inside items
                contentPadding = PaddingValues(
                    top = dynamicTopPadding + WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = 110.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + if (totalItems > 0) 80.dp else 0.dp // Extra padding if cart panel is shown
                ),
                verticalArrangement = Arrangement.spacedBy(spacerMedium)
            ) {
                item(key = "order_type") {
                    SegmentedControl(
                        items = listOf("Restaurant", "Delivery"),
                        selectedIndex = selectedOrderType,
                        onItemSelected = { selectedOrderType = it },
                        modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
                    )
                }
                
                item(key = "search_bar") {
                    AppSearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
                    )
                }
                
                item(key = "categories") {
                    CategoryChips(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.selectCategory(it) }
                    )
                }
                
                if (filteredMenuItems.isEmpty()) {
                    item(key = "empty_state") {
                        Spacer(modifier = Modifier.height(spacerLarge))
                        EmptyState(
                            title = "No items found",
                            subtitle = "Try adjusting your search or category."
                        )
                    }
                } else {
                    items(filteredMenuItems, key = { it.id }) { item ->
                        val quantity = cart.find { it.menuItem.id == item.id }?.quantity ?: 0
                        MenuItemCard(
                            menuItem = item,
                            quantity = quantity,
                            selectedOrderType = selectedOrderType,
                            onAddClick = { viewModel.addToCart(item) },
                            onRemoveClick = { viewModel.updateQuantity(item.id, -1) },
                            modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
                        )
                    }
                }
            }
        }
        },
        overlayContent = {
            PremiumBottomSheet(
                visible = totalItems > 0,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 84.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp)
                    .padding(horizontal = AppTheme.spacing.lg)
            ) {
                    if (isCartExpanded) {
                        // Expanded view
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = screenHeight * 0.75f)
                                .padding(16.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(cart) { cartItem ->
                                    Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(AppTheme.radius.sm)
                                            .background(AppTheme.colors.surfaceLighter),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ShoppingCart,
                                            contentDescription = null,
                                            tint = AppTheme.colors.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cartItem.menuItem.name,
                                            style = AppTheme.typography.bodyMedium,
                                            color = AppTheme.colors.textPrimary
                                        )
                                        val itemPrice = if (selectedOrderType == 1 && cartItem.menuItem.deliveryPrice != null) cartItem.menuItem.deliveryPrice else cartItem.menuItem.price
                                        Text(
                                            text = "${cartItem.quantity} x " + com.example.util.CurrencyFormatter.format(itemPrice),
                                            style = AppTheme.typography.labelMedium,
                                            color = AppTheme.colors.textSecondary
                                        )
                                    }
                                    
                                    val itemTotal = cartItem.quantity * (if (selectedOrderType == 1 && cartItem.menuItem.deliveryPrice != null) cartItem.menuItem.deliveryPrice else cartItem.menuItem.price)
                                    Text(
                                        text = com.example.util.CurrencyFormatter.format(itemTotal),
                                        style = AppTheme.typography.titleMedium,
                                        color = AppTheme.colors.textPrimary
                                    )
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(AppTheme.colors.surfaceLighter)
                                            .bouncyClickable { viewModel.updateQuantity(cartItem.menuItem.id, -1) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Remove,
                                            contentDescription = "Remove",
                                            tint = AppTheme.colors.textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.1f))
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Subtotal",
                                    style = AppTheme.typography.bodyLarge,
                                    color = AppTheme.colors.textSecondary
                                )
                                Text(
                                    text = com.example.util.CurrencyFormatter.format(subtotal),
                                    style = AppTheme.typography.titleMedium,
                                    color = AppTheme.colors.textPrimary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "GST (${currentGstRate}%)",
                                        style = AppTheme.typography.bodyLarge,
                                        color = AppTheme.colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = isGstEnabled,
                                        onCheckedChange = { isGstEnabled = it },
                                        modifier = Modifier.scale(0.8f),
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = AppTheme.colors.background,
                                            checkedTrackColor = AppTheme.colors.accent,
                                            uncheckedThumbColor = AppTheme.colors.textSecondary,
                                            uncheckedTrackColor = AppTheme.colors.surfaceLighter,
                                            uncheckedBorderColor = AppTheme.colors.borderLight
                                        )
                                    )
                                }
                                Text(
                                    text = com.example.util.CurrencyFormatter.format(animatedGst),
                                    style = AppTheme.typography.titleMedium,
                                    color = AppTheme.colors.textPrimary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total",
                                    style = AppTheme.typography.bodyLarge,
                                    color = AppTheme.colors.textPrimary
                                )
                                Text(
                                    text = com.example.util.CurrencyFormatter.format(animatedTotal),
                                    style = AppTheme.typography.titleLarge,
                                    color = AppTheme.colors.textPrimary
                                )
                            }

                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(AppTheme.radius.md)
                                    .background(AppTheme.colors.accent)
                                    .bouncyClickable {
                                        viewModel.checkout(selectedOrderType, isGstEnabled)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Print,
                                        contentDescription = null,
                                        tint = AppTheme.colors.iconDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Save & Print",
                                        style = AppTheme.typography.labelMedium,
                                        color = AppTheme.colors.iconDark
                                    )
                                }
                            }
                        }
                        }
                    }
                    
                    // Compact header (always visible when totalItems > 0)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .bouncyClickable { if (!isCartExpanded) isCartExpanded = true }
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(32.dp),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ShoppingCart,
                                    contentDescription = null,
                                    tint = AppTheme.colors.textSecondary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.CenterStart)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(AppTheme.colors.accent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = totalItems.toString(),
                                        style = AppTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                        color = AppTheme.colors.iconDark
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$totalItems items",
                                style = AppTheme.typography.bodyLarge,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .bouncyClickable { showClearCartDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear Cart",
                                    tint = AppTheme.colors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = com.example.util.CurrencyFormatter.format(subtotal),
                                style = AppTheme.typography.titleMedium,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .bouncyClickable { 
                                        if (isCartExpanded) isCartExpanded = false else isCartExpanded = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCartExpanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                                    contentDescription = null,
                                    tint = AppTheme.colors.textSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
            }
            if (showClearCartDialog) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { showClearCartDialog = false },
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
                                text = "Clear Cart",
                                style = AppTheme.typography.titleLarge,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                            Text(
                                text = "Clear all items from this bill?",
                                style = AppTheme.typography.bodyLarge,
                                color = AppTheme.colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                            
                            PrimaryButton(
                                text = "Clear",
                                containerColor = androidx.compose.ui.graphics.Color(0xFFFF4C4C),
                                onClick = {
                                    viewModel.clearCart()
                                    showClearCartDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(AppTheme.radius.md)
                                    .border(1.dp, AppTheme.colors.borderLight, AppTheme.radius.md)
                                    .liftClickable { showClearCartDialog = false },
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
    )
    
        // Toast
        AnimatedVisibility(
            visible = showToast,
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
                Text(toastMessage, style = AppTheme.typography.labelMedium, color = AppTheme.colors.textPrimary)
            }
        }
    }

    if (printEvent !is PrintEvent.None) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { if (printEvent !is PrintEvent.Printing) viewModel.clearPrintEvent() },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = printEvent !is PrintEvent.Printing,
                dismissOnClickOutside = printEvent !is PrintEvent.Printing,
                usePlatformDefaultWidth = false
            )
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val event = printEvent) {
                        is PrintEvent.Printing -> {
                            androidx.compose.material3.CircularProgressIndicator(color = AppTheme.colors.accent)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Printing...",
                                style = AppTheme.typography.titleLarge,
                                color = AppTheme.colors.textPrimary
                            )
                        }
                        is PrintEvent.Success -> {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Print Successful",
                                style = AppTheme.typography.titleLarge,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            com.example.ui.components.PrimaryButton(
                                text = "OK",
                                onClick = { viewModel.clearPrintEvent() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is PrintEvent.Offline -> {
                            Icon(
                                imageVector = Icons.Rounded.PrintDisabled,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Printer Offline",
                                style = AppTheme.typography.titleLarge,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This order has been saved successfully.\nYou can reprint it later from Order History.",
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            com.example.ui.components.PrimaryButton(
                                text = "OK",
                                onClick = { viewModel.clearPrintEvent() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is PrintEvent.NoPrinter -> {
                            Icon(
                                imageVector = Icons.Rounded.PrintDisabled,
                                contentDescription = null,
                                tint = AppTheme.colors.textSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Printer Connected",
                                style = AppTheme.typography.titleLarge,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Bill saved. Please connect a printer in settings.",
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            com.example.ui.components.PrimaryButton(
                                text = "OK",
                                onClick = { viewModel.clearPrintEvent() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is PrintEvent.Failed -> {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Print Failed",
                                style = AppTheme.typography.titleLarge,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Reason: ${event.message}",
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                com.example.ui.components.PrimaryButton(containerColor = AppTheme.colors.surfaceLighter, contentColor = AppTheme.colors.textPrimary,
                                    text = "Cancel",
                                    onClick = { viewModel.clearPrintEvent() },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                com.example.ui.components.PrimaryButton(
                                    text = "OK",
                                    onClick = { viewModel.clearPrintEvent() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
