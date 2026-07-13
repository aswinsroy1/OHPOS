import re

with open('app/src/main/java/com/example/ui/screens/HomeViewModel.kt', 'r') as f:
    content = f.read()

new_print = '''    fun printBill(context: android.content.Context, billWithItems: BillWithItems) {
        viewModelScope.launch {
            val printerDao = AppDatabase.getDatabase(getApplication()).printerDao()
            val defaultPrinter = printerDao.getDefaultPrinterSync()
            if (defaultPrinter != null) {
                val success = com.example.util.PrinterManager.printReceipt(
                    context,
                    defaultPrinter,
                    billWithItems.bill,
                    billWithItems.items
                )
                billDao.updatePrintStatus(billWithItems.bill.id, if (success) "PRINTED" else "PRINT_FAILED")
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (success) {
                        android.widget.Toast.makeText(context, "✓ Bill printed successfully", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Printing Failed", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                billDao.updatePrintStatus(billWithItems.bill.id, "SAVED")
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Bill saved. Printer not connected.", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }'''

content = re.sub(r'fun printBill\(context: android\.content\.Context, billWithItems: BillWithItems\) \{.*?\n    \}', new_print, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/HomeViewModel.kt', 'w') as f:
    f.write(content)
