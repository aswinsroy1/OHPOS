import re

with open('app/src/main/java/com/example/ui/screens/PrinterViewModel.kt', 'r') as f:
    content = f.read()

target = """    fun testPrint(context: Context, printer: SavedPrinter) {
        viewModelScope.launch {
            _isPrinting.value = true
            val success = PrinterManager.printTest(context, printer)
            _isPrinting.value = false
            if (!success) {
                _printError.value = "Failed to print test page"
            }
        }
    }"""

replacement = """    fun testPrint(context: Context, printer: SavedPrinter) {
        viewModelScope.launch {
            val currentStatus = com.example.util.PrinterStatusMonitor.statuses.value[printer.id]?.state
            if (currentStatus != com.example.util.PrinterState.CONNECTED) {
                _printError.value = "Printer Offline\\nUnable to send test print because the printer is currently unavailable."
                return@launch
            }
            
            _isPrinting.value = true
            val success = com.example.util.PrinterManager.printTest(context, printer)
            _isPrinting.value = false
            if (!success) {
                _printError.value = "Failed to print test page"
            }
        }
    }"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/PrinterViewModel.kt', 'w') as f:
    f.write(content)
