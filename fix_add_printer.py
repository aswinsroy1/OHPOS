import re

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "r") as f:
    content = f.read()

replacement = """
@Composable
fun AddPrinterDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var step by remember { mutableStateOf(1) } // 1: Type, 2: Details/Scan
    var type by remember { mutableStateOf("WIFI") }
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var paperWidth by remember { mutableStateOf(58) }

    com.example.ui.components.PremiumBottomSheet(
        onDismiss = onDismiss,
        title = if (step == 1) "Printer Type" else "Add Printer",
        subtitle = if (step == 1) "Select connection method" else "Configure details"
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md)) {
            if (step == 1) {
                listOf("BLUETOOTH" to "Bluetooth", "USB" to "USB OTG", "WIFI" to "Network / Wi-Fi").forEach { (typeKey, typeLabel) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().liftClickable {
                            type = typeKey
                            step = 2
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
            } else {
                if (type == "BLUETOOTH") {
                    Text("Paired Devices", style = AppTheme.typography.titleMedium, color = AppTheme.colors.textPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    val paired = remember { com.example.util.PrinterManager.getPairedBluetoothPrinters(context) }
                    if (paired.isEmpty()) {
                        Text("No paired Bluetooth devices found.", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                    } else {
                        paired.forEach { device ->
                            Row(
                                modifier = Modifier.fillMaxWidth().liftClickable {
                                    name = device.name ?: "Unknown Bluetooth Printer"
                                    address = device.address
                                }.padding(vertical = AppTheme.spacing.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.RadioButton(selected = address == device.address, onClick = {
                                    name = device.name ?: "Unknown Bluetooth Printer"
                                    address = device.address
                                })
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(device.name ?: "Unknown", style = AppTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
                                    Text(device.address, style = AppTheme.typography.bodySmall, color = AppTheme.colors.textSecondary)
                                }
                            }
                        }
                    }
                } else {
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
                        label = if (type == "WIFI") "IP Address (e.g. 192.168.1.100)" else "USB Identifier",
                        icon = Icons.Rounded.Link
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Paper Width", style = AppTheme.typography.titleMedium, color = AppTheme.colors.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.RadioButton(selected = paperWidth == 58, onClick = { paperWidth = 58 })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("58mm", color = AppTheme.colors.textPrimary)
                    Spacer(modifier = Modifier.width(24.dp))
                    androidx.compose.material3.RadioButton(selected = paperWidth == 80, onClick = { paperWidth = 80 })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("80mm", color = AppTheme.colors.textPrimary)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                com.example.ui.components.PrimaryButton(
                    text = "Save Printer",
                    onClick = {
                        if (name.isNotBlank() && address.isNotBlank()) {
                            onAdd(name, type, address, paperWidth)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
"""

content = re.sub(
    r"@Composable\nfun AddPrinterDialog\([\s\S]*",
    replacement,
    content
)

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "w") as f:
    f.write(content)
