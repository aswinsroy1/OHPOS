import re

with open('app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt', 'r') as f:
    content = f.read()

# Replace `val isConnected by viewModel.getConnectionStatus(printer.id).collectAsState(initial = false)` 
# with `val statuses by com.example.util.PrinterStatusMonitor.statuses.collectAsState()`
content = re.sub(
    r"val isConnected by viewModel\.getConnectionStatus\(printer\.id\)\.collectAsState\(initial = false\)",
    r"val printerStatuses by com.example.util.PrinterStatusMonitor.statuses.collectAsState()\n                    val printerStatus = printerStatuses[printer.id]",
    content
)

content = re.sub(
    r"isConnected = isConnected,",
    r"printerStatus = printerStatus,",
    content
)

with open('app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt', 'w') as f:
    f.write(content)

