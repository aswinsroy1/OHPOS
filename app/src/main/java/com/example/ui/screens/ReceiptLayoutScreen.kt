package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.components.*
import com.example.ui.theme.AppTheme
import androidx.compose.ui.platform.LocalContext
import com.example.data.PrinterPreferencesRepository
import kotlinx.coroutines.launch

@Composable
fun ReceiptLayoutScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val prefRepo = remember { PrinterPreferencesRepository(context) }
    val scope = rememberCoroutineScope()
    
    val resName by prefRepo.resNameFlow.collectAsState(initial = "")
    val resAddress by prefRepo.resAddressFlow.collectAsState(initial = "")
    val resPhone by prefRepo.resPhoneFlow.collectAsState(initial = "")
    val resGst by prefRepo.resGstFlow.collectAsState(initial = "")
    val invoiceFooter by prefRepo.invoiceFooterFlow.collectAsState(initial = "")
    val thankYouMsg by prefRepo.thankYouMsgFlow.collectAsState(initial = "")
    
    val printDate by prefRepo.printDateFlow.collectAsState(initial = true)
    val printTime by prefRepo.printTimeFlow.collectAsState(initial = true)
    val printCashier by prefRepo.printCashierFlow.collectAsState(initial = true)
    val printPaymentMethod by prefRepo.printPaymentMethodFlow.collectAsState(initial = true)
    val printQr by prefRepo.printQrFlow.collectAsState(initial = true)
    val printOrderType by prefRepo.printOrderTypeFlow.collectAsState(initial = true)
    val printItemNotes by prefRepo.printItemNotesFlow.collectAsState(initial = true)
    val printGstBreakdown by prefRepo.printGstBreakdownFlow.collectAsState(initial = true)
    val printDiscount by prefRepo.printDiscountFlow.collectAsState(initial = true)
    val printCustomerName by prefRepo.printCustomerNameFlow.collectAsState(initial = true)
    val printCustomerPhone by prefRepo.printCustomerPhoneFlow.collectAsState(initial = true)

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
                title = "Receipt Layout",
                onBackClick = onBackClick,
                modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = AppTheme.spacing.lg,
                    end = AppTheme.spacing.lg,
                    bottom = 120.dp,
                    top = AppTheme.spacing.md
                )
            ) {
                item {
                    SectionHeader(title = "Header Settings")
                    
                    StatefulTextField(
                        value = resName,
                        onValueChange = { scope.launch { prefRepo.setResName(it) } },
                        label = "Restaurant Name",
                        icon = Icons.Rounded.Storefront
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                    StatefulTextField(
                        value = resAddress,
                        onValueChange = { scope.launch { prefRepo.setResAddress(it) } },
                        label = "Address",
                        icon = Icons.Rounded.Place
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                    StatefulTextField(
                        value = resPhone,
                        onValueChange = { scope.launch { prefRepo.setResPhone(it) } },
                        label = "Phone Number",
                        icon = Icons.Rounded.Phone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                    StatefulTextField(
                        value = resGst,
                        onValueChange = { scope.launch { prefRepo.setResGst(it) } },
                        label = "GST Number",
                        icon = Icons.Rounded.AccountBalance
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                }
                
                item {
                    SectionHeader(title = "Visibility Toggles")
                    
                    LayoutToggle("Print Date", printDate) { scope.launch { prefRepo.setPrintDate(it) } }
                    LayoutToggle("Print Time", printTime) { scope.launch { prefRepo.setPrintTime(it) } }
                    LayoutToggle("Print Cashier Name", printCashier) { scope.launch { prefRepo.setPrintCashier(it) } }
                    LayoutToggle("Print Payment Method", printPaymentMethod) { scope.launch { prefRepo.setPrintPaymentMethod(it) } }
                    LayoutToggle("Print QR Code", printQr) { scope.launch { prefRepo.setPrintQr(it) } }
                    LayoutToggle("Print Order Type", printOrderType) { scope.launch { prefRepo.setPrintOrderType(it) } }
                    LayoutToggle("Print Item Notes", printItemNotes) { scope.launch { prefRepo.setPrintItemNotes(it) } }
                    LayoutToggle("Print GST Breakdown", printGstBreakdown) { scope.launch { prefRepo.setPrintGstBreakdown(it) } }
                    LayoutToggle("Print Discount", printDiscount) { scope.launch { prefRepo.setPrintDiscount(it) } }
                    LayoutToggle("Print Customer Name", printCustomerName) { scope.launch { prefRepo.setPrintCustomerName(it) } }
                    LayoutToggle("Print Customer Phone", printCustomerPhone) { scope.launch { prefRepo.setPrintCustomerPhone(it) } }
                    
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                }
                
                item {
                    SectionHeader(title = "Footer Settings")
                    
                    StatefulTextField(
                        value = invoiceFooter,
                        onValueChange = { scope.launch { prefRepo.setInvoiceFooter(it) } },
                        label = "Invoice Footer Note",
                        icon = Icons.Rounded.Notes
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                    StatefulTextField(
                        value = thankYouMsg,
                        onValueChange = { scope.launch { prefRepo.setThankYouMsg(it) } },
                        label = "Thank You Message",
                        icon = Icons.Rounded.Favorite
                    )
                }
            }
        }
    }
}

@Composable
fun LayoutToggle(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = AppTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun StatefulTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var localValue by remember { mutableStateOf(value) }
    var isFocused by remember { mutableStateOf(false) }

    val currentLocalValue by rememberUpdatedState(localValue)
    val currentIsFocused by rememberUpdatedState(isFocused)
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    LaunchedEffect(value) {
        if (!isFocused) {
            localValue = value
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (currentIsFocused && currentLocalValue != value) {
                currentOnValueChange(currentLocalValue)
            }
        }
    }

    PremiumTextField(
        value = localValue,
        onValueChange = { localValue = it },
        label = label,
        icon = icon,
        keyboardOptions = keyboardOptions,
        modifier = Modifier.onFocusChanged { state ->
            if (isFocused && !state.isFocused && localValue != value) {
                onValueChange(localValue)
            }
            isFocused = state.isFocused
        }
    )
}
