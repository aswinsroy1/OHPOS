import re

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "r") as f:
    content = f.read()

# Update PrinterCard signature
card_sig_old = """fun PrinterCard(
    printer: SavedPrinter,
    onTestPrint: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
)"""
card_sig_new = """fun PrinterCard(
    printer: SavedPrinter,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onRename: () -> Unit,
    onTestPrint: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
)"""
content = content.replace(card_sig_old, card_sig_new)

# Find PrinterCard usages and update them
# It is used in PrinterSettingsScreen:
# items(savedPrinters) { printer ->
#    PrinterCard(
#        printer = printer,
#        onTestPrint = { printerViewModel.testPrint(context, printer) },
#        onSetDefault = { printerViewModel.setDefaultPrinter(printer) },
#        onDelete = { printerViewModel.deletePrinter(printer) }
#    )
# }
usage_old = """items(savedPrinters) { printer ->
                    PrinterCard(
                        printer = printer,
                        onTestPrint = { viewModel.testPrint(context, printer) },
                        onSetDefault = { viewModel.setDefaultPrinter(printer) },
                        onDelete = { viewModel.deletePrinter(printer) }
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                }"""
usage_new = """items(savedPrinters) { printer ->
                    val isConnected by viewModel.getConnectionStatus(printer.id).collectAsState(initial = false)
                    PrinterCard(
                        printer = printer,
                        isConnected = isConnected,
                        onConnect = { viewModel.connectPrinter(printer) },
                        onDisconnect = { viewModel.disconnectPrinter(printer) },
                        onRename = { /* Handle rename */ },
                        onTestPrint = { viewModel.testPrint(context, printer) },
                        onSetDefault = { viewModel.setDefaultPrinter(printer) },
                        onDelete = { viewModel.deletePrinter(printer) }
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                }"""
content = content.replace(usage_old, usage_new)
# Let's use regex in case whitespace is different
content = re.sub(
    r'items\(savedPrinters\) \{ printer ->\s*PrinterCard\(\s*printer = printer,\s*onTestPrint = \{[^\}]*\},\s*onSetDefault = \{[^\}]*\},\s*onDelete = \{[^\}]*\}\s*\)\s*Spacer\(modifier = Modifier.height\(AppTheme.spacing.md\)\)\s*\}',
    usage_new,
    content
)


# Rewrite PrinterCard content
start_card = content.find("fun PrinterCard(")
if start_card != -1:
    end_card = content.find("@Composable\nfun LayoutToggle", start_card)
    if end_card == -1:
        end_card = len(content)
    
    new_card = """fun PrinterCard(
    printer: SavedPrinter,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onRename: () -> Unit,
    onTestPrint: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
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
            .liftClickable { expanded = true }
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
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Connection Status
                    Box(
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(if (isConnected) Color(0xFF4CAF50).copy(alpha=0.2f) else AppTheme.colors.error.copy(alpha=0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isConnected) "Connected" else "Offline",
                            style = AppTheme.typography.labelSmall,
                            color = if (isConnected) Color(0xFF4CAF50) else AppTheme.colors.error
                        )
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
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.accent
                    )
                }
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(AppTheme.colors.surface)
        ) {
            if (isConnected) {
                DropdownMenuItem(
                    text = { Text("Disconnect", color = AppTheme.colors.textPrimary) },
                    onClick = { onDisconnect(); expanded = false },
                    leadingIcon = { Icon(Icons.Rounded.CloudOff, null, tint = AppTheme.colors.textSecondary) }
                )
            } else {
                DropdownMenuItem(
                    text = { Text("Connect", color = AppTheme.colors.textPrimary) },
                    onClick = { onConnect(); expanded = false },
                    leadingIcon = { Icon(Icons.Rounded.CloudQueue, null, tint = AppTheme.colors.textSecondary) }
                )
            }
            DropdownMenuItem(
                text = { Text("Set as Default", color = AppTheme.colors.textPrimary) },
                onClick = { onSetDefault(); expanded = false },
                leadingIcon = { Icon(Icons.Rounded.Star, null, tint = AppTheme.colors.textSecondary) }
            )
            DropdownMenuItem(
                text = { Text("Test Print", color = AppTheme.colors.textPrimary) },
                onClick = { onTestPrint(); expanded = false },
                leadingIcon = { Icon(Icons.Rounded.Print, null, tint = AppTheme.colors.textSecondary) }
            )
            DropdownMenuItem(
                text = { Text("Rename", color = AppTheme.colors.textPrimary) },
                onClick = { onRename(); expanded = false },
                leadingIcon = { Icon(Icons.Rounded.Edit, null, tint = AppTheme.colors.textSecondary) }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = AppTheme.colors.error) },
                onClick = { onDelete(); expanded = false },
                leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = AppTheme.colors.error) }
            )
        }
    }
}
"""
    content = content[:start_card] + new_card + content[end_card:]

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "w") as f:
    f.write(content)
