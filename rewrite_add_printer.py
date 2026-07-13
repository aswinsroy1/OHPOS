import re

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "r") as f:
    content = f.read()

# I will replace the entire AddPrinterDialog with a new version.
# To do that, I'll find its boundaries.
start_idx = content.find("@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun AddPrinterDialog")
if start_idx == -1:
    start_idx = content.find("@Composable\nfun AddPrinterDialog")

end_idx = content.find("@Composable\nfun PrinterCard")
if end_idx == -1:
    print("Could not find end of AddPrinterDialog")
    exit(1)

new_dialog = """@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun AddPrinterDialog(
    onDismissRequest: () -> Unit,
    onAdd: (String, String, String, Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var step by remember { mutableStateOf(1) } // 1: Type, 2: Discover/Details
    var type by remember { mutableStateOf("WIFI") }
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var paperWidth by remember { mutableStateOf(58) }

    val discoveryHelper = remember { com.example.util.DiscoveryHelper(context) }
    val discoveredPrinters by discoveryHelper.discoveredPrinters.collectAsState()

    DisposableEffect(type, step) {
        if (step == 2) {
            discoveryHelper.clear()
            when (type) {
                "WIFI" -> discoveryHelper.startNetworkDiscovery()
                "USB" -> discoveryHelper.discoverUsbPrinters()
                "BLUETOOTH" -> discoveryHelper.startBluetoothDiscovery()
            }
        }
        onDispose {
            if (step == 2) {
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
        // Permissions granted or denied, we proceed anyway
        step = 2
    }

    if (step == 1) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            containerColor = AppTheme.colors.surface,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md)) {
                Text("Printer Type", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
                Text("Select connection method", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                listOf("BLUETOOTH" to "Bluetooth", "USB" to "USB OTG", "WIFI" to "Network / Wi-Fi").forEach { (typeKey, typeLabel) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().liftClickable {
                            type = typeKey
                            if (typeKey == "BLUETOOTH") {
                                launcher.launch(permissions.toTypedArray())
                            } else {
                                step = 2
                            }
                        }.padding(vertical = AppTheme.spacing.md),
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
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    } else {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            containerColor = AppTheme.colors.surface,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = AppTheme.spacing.lg, end = AppTheme.spacing.lg, top = AppTheme.spacing.md, bottom = 32.dp)
            ) {
                item {
                    Text("Add Printer", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
                    Text("Select a discovered printer or enter manually", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (discoveredPrinters.isNotEmpty()) {
                    item {
                        Text("Discovered Printers", style = AppTheme.typography.titleMedium, color = AppTheme.colors.textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(discoveredPrinters) { device ->
                        Row(
                            modifier = Modifier.fillMaxWidth().liftClickable {
                                name = device.name
                                address = device.address
                            }.padding(vertical = AppTheme.spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = address == device.address, 
                                onClick = {
                                    name = device.name
                                    address = device.address
                                },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                    selectedColor = AppTheme.colors.accent,
                                    unselectedColor = AppTheme.colors.textSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(device.name, style = AppTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
                                Text("${device.extraInfo} • ${device.address}", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                item {
                    Text("Manual Entry", style = AppTheme.typography.titleMedium, color = AppTheme.colors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
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
                    
                    Spacer(modifier = Modifier.height(24.dp))
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
                }
            }
        }
    }
}
"""

content = content[:start_idx] + new_dialog + content[end_idx:]

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "w") as f:
    f.write(content)
