import re

with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'r') as f:
    content = f.read()

top_declarations = """    val printerStatuses by com.example.util.PrinterStatusMonitor.statuses.collectAsState()
    val defaultPrinterStatus = defaultPrinter?.id?.let { printerStatuses[it] }?.state ?: com.example.util.PrinterState.DISCONNECTED"""

new_declarations = top_declarations + """
    
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    
    LaunchedEffect(showToast) {
        if (showToast) {
            kotlinx.coroutines.delay(3000)
            showToast = false
        }
    }
"""

content = content.replace(top_declarations, new_declarations)

with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'w') as f:
    f.write(content)
