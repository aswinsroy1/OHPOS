import re

with open('app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt', 'r') as f:
    content = f.read()

# Replace the whole PrinterCard function.
# It starts at `fun PrinterCard(` and ends at the end of the file or before the next top-level function.

new_func = """fun PrinterCard(
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
            dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle(color = AppTheme.colors.textMuted) }
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
                    com.example.ui.components.SecondaryButton(
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
                
                com.example.ui.components.DangerButton(
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
"""

start_idx = content.find("fun PrinterCard(")
if start_idx != -1:
    content = content[:start_idx] + new_func

with open('app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt', 'w') as f:
    f.write(content)
