package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.rounded.PrintDisabled
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import kotlinx.coroutines.launch
import com.example.data.PrinterPreferencesRepository
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.components.*
import com.example.ui.theme.AppTheme
import com.example.data.SavedPrinter

import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun PrinterSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: PrinterViewModel = viewModel()
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    val savedPrinters by viewModel.savedPrinters.collectAsState()
    val isPrinting by viewModel.isPrinting.collectAsState()
    val printError by viewModel.printError.collectAsState()

    val context = LocalContext.current
    val prefRepo = remember { PrinterPreferencesRepository(context) }
    val printCustomerCopy by prefRepo.printCustomerCopyFlow.collectAsState(initial = false)
    val printKitchenCopy by prefRepo.printKitchenCopyFlow.collectAsState(initial = false)
    val openDrawer by prefRepo.openDrawerFlow.collectAsState(initial = false)
    val scope = rememberCoroutineScope()




    if (printError != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewModel.clearError() },
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (printError!!.contains("Offline")) Icons.Rounded.PrintDisabled else Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (printError!!.contains("Offline")) "Printer Offline" else "Error",
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = printError!!.replace("Printer Offline\n", ""),
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    com.example.ui.components.PrimaryButton(
                        text = "OK",
                        onClick = { viewModel.clearError() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
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
                title = "Printer Settings",
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
                    SectionHeader(
                        title = "Saved Printers",
                        actionText = "+ Add Printer",
                        onActionClick = { showAddDialog = true }
                    )
                }
                
                if (savedPrinters.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No printers found",
                            subtitle = "Add a USB, Bluetooth or Wi-Fi printer.",
                            icon = Icons.Rounded.Print
                        )
                    }
                } else {
                    items(savedPrinters) { printer ->
                    val printerStatuses by com.example.util.PrinterStatusMonitor.statuses.collectAsState()
                    val printerStatus = printerStatuses[printer.id]
                    PrinterCard(
                        printer = printer,
                        printerStatus = printerStatus,
                        onConnect = { viewModel.connectPrinter(printer) },
                        onDisconnect = { viewModel.disconnectPrinter(printer) },
                        onRename = { /* Handle rename */ },
                        onTestPrint = { viewModel.testPrint(context, printer) },
                        onSetDefault = { viewModel.setDefaultPrinter(printer) },
                        onDelete = { viewModel.deletePrinter(printer) }
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                }
                }
                
                item {
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    SectionHeader(title = "Auto Print")
                    
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
                            SettingsToggleRow(title = "Print Customer Copy", icon = Icons.Rounded.Receipt, checked = printCustomerCopy, onCheckedChange = { scope.launch { prefRepo.setPrintCustomerCopy(it) } })
                            SettingsToggleRow(title = "Print Kitchen Copy", icon = Icons.Rounded.Restaurant, checked = printKitchenCopy, onCheckedChange = { scope.launch { prefRepo.setPrintKitchenCopy(it) } })
                            SettingsToggleRow(title = "Open Drawer Automatically", icon = Icons.Rounded.PointOfSale, checked = openDrawer, onCheckedChange = { scope.launch { prefRepo.setOpenDrawer(it) } })
                        }
                    }
                }
            }
        }
        
        if (showAddDialog) {
            AddPrinterDialog(
                onDismissRequest = { showAddDialog = false },
                onAdd = { name, type, address, paperWidth ->
                    viewModel.addPrinter(name, type, address, paperWidth, savedPrinters.isEmpty())
                }
            )
        }
    }
}


@Composable
fun SettingsToggleRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
                imageVector = icon,
                contentDescription = null,
                tint = AppTheme.colors.textSecondary,
                modifier = Modifier.size(AppTheme.dimensions.iconSizeMd)
            )
        }
        Spacer(modifier = Modifier.width(AppTheme.spacing.md))
        Text(
            text = title,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}


@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun AddPrinterDialog(
    onDismissRequest: () -> Unit,
    onAdd: (String, String, String, Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var step by remember { mutableStateOf(1) } // 1: Type, 2: Scanning, 3: Discovered List, 4: Save Printer, 5: Manual Entry
    var type by remember { mutableStateOf("WIFI") }
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var extraInfo by remember { mutableStateOf("") }
    var paperWidth by remember { mutableStateOf(58) }

    val discoveryHelper = remember { com.example.util.DiscoveryHelper(context) }
    val discoveredPrinters by discoveryHelper.discoveredPrinters.collectAsState()

    LaunchedEffect(type, step) {
        if (step == 2) {
            discoveryHelper.clear()
            when (type) {
                "WIFI" -> discoveryHelper.startNetworkDiscovery()
                "USB" -> discoveryHelper.discoverUsbPrinters()
                "BLUETOOTH" -> discoveryHelper.startBluetoothDiscovery()
            }
            kotlinx.coroutines.delay(1500)
            if (type == "USB") {
                if (discoveryHelper.discoveredPrinters.value.size == 1) {
                    val device = discoveryHelper.discoveredPrinters.value.first()
                    name = device.name
                    address = device.address
                    extraInfo = device.extraInfo
                    step = 4
                } else {
                    step = 3
                }
            } else {
                step = 3
            }
        }
    }
    
    DisposableEffect(type, step) {
        onDispose {
            if (step != 2 && step != 3) {
                when (type) {
                    "WIFI" -> discoveryHelper.stopNetworkDiscovery()
                    "BLUETOOTH" -> discoveryHelper.stopBluetoothDiscovery()
                }
            }
        }
    }

    // Permission handling for Bluetooth
    val permissions = remember {
        val list = mutableListOf<String>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            list.add(android.Manifest.permission.BLUETOOTH_CONNECT)
            list.add(android.Manifest.permission.BLUETOOTH_SCAN)
        } else {
            list.add(android.Manifest.permission.BLUETOOTH)
            list.add(android.Manifest.permission.BLUETOOTH_ADMIN)
            list.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        list
    }
    
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) {
        step = 2
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
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
            when (step) {
                1 -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Printer Type", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
                        Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                        Text("Select connection method", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                        listOf("BLUETOOTH" to "Bluetooth", "USB" to "USB OTG", "WIFI" to "Network / Wi-Fi").forEach { (typeKey, typeLabel) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(AppTheme.radius.md)
                                    .liftClickable {
                                        type = typeKey
                                        if (typeKey == "BLUETOOTH") {
                                            launcher.launch(permissions.toTypedArray())
                                        } else {
                                            step = 2
                                        }
                                    }
                                    .padding(vertical = AppTheme.spacing.md, horizontal = AppTheme.spacing.xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    when(typeKey) {
                                        "BLUETOOTH" -> Icons.Rounded.Bluetooth
                                        "USB" -> Icons.Rounded.Usb
                                        else -> Icons.Rounded.Wifi
                                    },
                                    contentDescription = null,
                                    tint = AppTheme.colors.textSecondary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(typeLabel, style = AppTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Rounded.ChevronRight, null, tint = AppTheme.colors.textSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(AppTheme.radius.md)
                                .liftClickable { onDismissRequest() },
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
                2 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = AppTheme.spacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(color = AppTheme.colors.accent)
                        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                        Text("Scanning for printers...", style = AppTheme.typography.titleMedium, color = AppTheme.colors.textPrimary)
                    }
                }
                3 -> {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        item {
                            Text("Discovered Printers", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
                            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                            Text("Select a printer to continue", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                        }
                        
                        if (discoveredPrinters.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = AppTheme.spacing.xl),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (type == "USB") "No USB printer detected." else "No printers detected.",
                                        style = AppTheme.typography.bodyLarge,
                                        color = AppTheme.colors.textSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    com.example.ui.components.PrimaryButton(
                                        text = "Scan Again",
                                        onClick = { step = 2 },
                                        modifier = Modifier.weight(1f),
                                        containerColor = AppTheme.colors.surfaceLighter,
                                        contentColor = AppTheme.colors.textPrimary
                                    )
                                    com.example.ui.components.PrimaryButton(
                                        text = "Add Manually",
                                        onClick = { step = 5 },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        } else {
                            items(discoveredPrinters) { device ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = AppTheme.spacing.md)
                                        .clip(AppTheme.radius.md)
                                        .background(AppTheme.colors.surfaceLighter)
                                        .liftClickable {
                                            name = device.name
                                            address = device.address
                                            extraInfo = device.extraInfo
                                            step = 4
                                        }
                                        .padding(AppTheme.spacing.md)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Print,
                                            contentDescription = null,
                                            tint = AppTheme.colors.accent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(device.name, style = AppTheme.typography.titleMedium, color = AppTheme.colors.textPrimary)
                                            Text("${device.extraInfo} • ${device.address}", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Detected", style = AppTheme.typography.labelMedium, color = Color(0xFF4CAF50))
                                    }
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Can't find your printer?", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Icon(Icons.Rounded.ArrowDownward, contentDescription = null, tint = AppTheme.colors.textSecondary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (type == "WIFI") "Enter IP Address Manually" else "Add Manually",
                                            style = AppTheme.typography.titleMedium,
                                            color = AppTheme.colors.accent,
                                            modifier = Modifier.liftClickable { step = 5 }.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Save Printer", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
                        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(AppTheme.radius.md)
                                .background(AppTheme.colors.surfaceLighter)
                                .padding(AppTheme.spacing.md)
                        ) {
                            Column {
                                Text("Printer Details", style = AppTheme.typography.labelMedium, color = AppTheme.colors.textSecondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(name, style = AppTheme.typography.titleMedium, color = AppTheme.colors.textPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(address, style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                                if (extraInfo.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(extraInfo, style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Connection verified", style = AppTheme.typography.labelMedium, color = Color(0xFF4CAF50))
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                        Text("Paper Width", style = AppTheme.typography.titleMedium, color = AppTheme.colors.textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.RadioButton(
                                selected = paperWidth == 58, 
                                onClick = { paperWidth = 58 },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                    selectedColor = AppTheme.colors.accent,
                                    unselectedColor = AppTheme.colors.textSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("58 mm", color = AppTheme.colors.textPrimary)
                            Spacer(modifier = Modifier.width(24.dp))
                            androidx.compose.material3.RadioButton(
                                selected = paperWidth == 80, 
                                onClick = { paperWidth = 80 },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                    selectedColor = AppTheme.colors.accent,
                                    unselectedColor = AppTheme.colors.textSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("80 mm", color = AppTheme.colors.textPrimary)
                        }
                        
                        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                        com.example.ui.components.PrimaryButton(
                            text = "Save Printer",
                            onClick = {
                                if (name.isNotBlank() && address.isNotBlank()) {
                                    onAdd(name, type, address, paperWidth)
                                    onDismissRequest()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(AppTheme.radius.md)
                                .liftClickable { step = 3 },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Back",
                                style = AppTheme.typography.titleMedium,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                    }
                }
                5 -> {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        item {
                            Text("Manual Entry", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
                            Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                            Text("Enter printer details manually", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                            
                            com.example.ui.components.PremiumTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = "Printer Name",
                                icon = Icons.Rounded.Print
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            com.example.ui.components.PremiumTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = if (type == "WIFI") "IP Address (e.g. 192.168.1.100)" else if (type == "BLUETOOTH") "MAC Address" else "USB Identifier",
                                icon = Icons.Rounded.Link
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Paper Width", style = AppTheme.typography.titleMedium, color = AppTheme.colors.textPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.RadioButton(
                                    selected = paperWidth == 58, 
                                    onClick = { paperWidth = 58 },
                                    colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                        selectedColor = AppTheme.colors.accent,
                                        unselectedColor = AppTheme.colors.textSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("58 mm", color = AppTheme.colors.textPrimary)
                                Spacer(modifier = Modifier.width(24.dp))
                                androidx.compose.material3.RadioButton(
                                    selected = paperWidth == 80, 
                                    onClick = { paperWidth = 80 },
                                    colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                        selectedColor = AppTheme.colors.accent,
                                        unselectedColor = AppTheme.colors.textSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("80 mm", color = AppTheme.colors.textPrimary)
                            }
                            
                            Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                            com.example.ui.components.PrimaryButton(
                                text = "Save Printer",
                                onClick = {
                                    if (name.isNotBlank() && address.isNotBlank()) {
                                        onAdd(name, type, address, paperWidth)
                                        onDismissRequest()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .clip(AppTheme.radius.md)
                                    .liftClickable { step = 3 },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Back",
                                    style = AppTheme.typography.titleMedium,
                                    color = AppTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
fun PrinterCard(
    printer: SavedPrinter,
    printerStatus: com.example.util.PrinterStatus?,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onRename: () -> Unit,
    onTestPrint: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }
    
    val state = printerStatus?.state ?: com.example.util.PrinterState.CONNECTING
    val statusColor = when (state) {
        com.example.util.PrinterState.CONNECTED -> Color(0xFF4CAF50)
        com.example.util.PrinterState.CONNECTING -> Color(0xFFFFC107)
        com.example.util.PrinterState.STANDBY -> Color(0xFF9E9E9E)
        com.example.util.PrinterState.OFFLINE -> Color(0xFFE53935)
        com.example.util.PrinterState.DISCONNECTED -> Color(0xFF424242)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = AppTheme.elevations.md,
                shape = AppTheme.radius.lg,
                spotColor = Color.Black,
                ambientColor = Color.Black
            )
            .clip(AppTheme.radius.lg)
            .background(AppTheme.colors.surface)
            .border(AppTheme.dimensions.borderThickness, Color.White.copy(alpha = 0.06f), AppTheme.radius.lg)
            .liftClickable { showDetails = true }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
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
                    imageVector = when(printer.type) {
                        "WIFI", "Network / Wi-Fi" -> Icons.Rounded.Wifi
                        "BLUETOOTH", "Bluetooth" -> Icons.Rounded.Bluetooth
                        else -> Icons.Rounded.Usb
                    },
                    contentDescription = null,
                    tint = AppTheme.colors.textSecondary,
                    modifier = Modifier.size(AppTheme.dimensions.iconSizeMd)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = printer.name,
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${printer.type} • ${printer.paperWidth}mm",
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Connection Status
                    Box(
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(androidx.compose.foundation.shape.CircleShape).background(statusColor))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = state.getDisplayName(),
                                style = AppTheme.typography.labelMedium,
                                color = statusColor
                            )
                        }
                    }
                }
            }
            
            if (printer.isDefault) {
            

    Box(
                    modifier = Modifier
                        .clip(AppTheme.radius.sm)
                        .background(AppTheme.colors.accent.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "DEFAULT",
                        style = AppTheme.typography.labelMedium,
                        color = AppTheme.colors.accent
                    )
                }
            }
        }
    }
    
    if (showDetails) {
        androidx.compose.material3.ExperimentalMaterial3Api::class
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showDetails = false },
            containerColor = AppTheme.colors.surface,
            dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle(color = AppTheme.colors.textSecondary) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Printer Details",
                    style = AppTheme.typography.titleLarge,
                    color = AppTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailRow("Name", printer.name)
                DetailRow("Connection Type", printer.type)
                DetailRow("Paper Width", "${printer.paperWidth} mm")
                DetailRow("Status", state.getDisplayName(), statusColor)
                DetailRow(if (printer.type == "WIFI") "IP Address" else if (printer.type == "BLUETOOTH") "MAC Address" else "USB ID", printer.address)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    com.example.ui.components.PrimaryButton(containerColor = AppTheme.colors.surfaceLighter, contentColor = AppTheme.colors.textPrimary,
                        text = "Test Print",
                        onClick = { onTestPrint(); showDetails = false },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    com.example.ui.components.PrimaryButton(
                        text = "Set Default",
                        onClick = { onSetDefault(); showDetails = false },
                        modifier = Modifier.weight(1f),
                        enabled = !printer.isDefault
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                com.example.ui.components.PrimaryButton(containerColor = androidx.compose.ui.graphics.Color(0xFFE53935), contentColor = androidx.compose.ui.graphics.Color.White,
                    text = "Delete Printer",
                    onClick = { onDelete(); showDetails = false },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color = AppTheme.colors.textPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
        Text(text = value, style = AppTheme.typography.titleMedium, color = valueColor)
    }
}
