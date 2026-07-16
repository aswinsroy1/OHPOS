import re

with open('app/src/main/java/com/example/ui/screens/ReportsViewModel.kt', 'r') as f:
    content = f.read()

if 'fun printBill' not in content:
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
    # insert before the last brace
    content = content.rstrip()
    if content.endswith('}'):
        content = content[:-1] + '\n' + new_print + '\n}'
        with open('app/src/main/java/com/example/ui/screens/ReportsViewModel.kt', 'w') as f:
            f.write(content)
