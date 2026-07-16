import re

with open('app/src/main/java/com/example/ui/screens/BillingViewModel.kt', 'r') as f:
    content = f.read()

# Replace the part inside printBill logic

target = """            if (defaultPrinter != null) {
                var success = true
                if (openDrawer) {
                    com.example.util.PrinterManager.openDrawer(defaultPrinter)
                }"""

replacement = """            if (defaultPrinter != null) {
                val currentStatus = com.example.util.PrinterStatusMonitor.statuses.value[defaultPrinter.id]?.state
                if (currentStatus != com.example.util.PrinterState.CONNECTED) {
                    _printEvent.value = PrintEvent.Offline(billId.toInt())
                    billDao.updatePrintStatus(billId.toInt(), "PRINT_FAILED")
                    return@launch
                }
                
                _printEvent.value = PrintEvent.Printing
                
                var success = true
                if (openDrawer) {
                    com.example.util.PrinterManager.openDrawer(defaultPrinter)
                }"""

content = content.replace(target, replacement)

target2 = """                billDao.updatePrintStatus(billId.toInt(), if (success) "PRINTED" else "PRINT_FAILED")
                
                if (success) {
                    _printEvent.value = PrintEvent.Success(billId.toInt())
                } else {
                    _printEvent.value = PrintEvent.Failed(billId.toInt())
                }
            } else {
                _printEvent.value = PrintEvent.NoPrinter(billId.toInt())
            }"""

replacement2 = """                billDao.updatePrintStatus(billId.toInt(), if (success) "PRINTED" else "PRINT_FAILED")
                
                if (success) {
                    _printEvent.value = PrintEvent.Success(billId.toInt())
                } else {
                    _printEvent.value = PrintEvent.Failed(billId.toInt(), "Printer disconnected during printing.\\nOrder has been saved.")
                }
            } else {
                _printEvent.value = PrintEvent.NoPrinter(billId.toInt())
            }"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/screens/BillingViewModel.kt', 'w') as f:
    f.write(content)

