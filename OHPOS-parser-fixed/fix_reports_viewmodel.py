import re

with open("app/src/main/java/com/example/ui/screens/ReportsViewModel.kt", "r") as f:
    content = f.read()

replacement = """
    fun printBill(context: android.content.Context, billWithItems: BillWithItems) {
        viewModelScope.launch {
            val printerDao = AppDatabase.getDatabase(getApplication()).printerDao()
            val defaultPrinter = printerDao.getDefaultPrinterSync()
            if (defaultPrinter != null) {
                val prefRepo = com.example.data.PrinterPreferencesRepository(context)
                val success = com.example.util.PrinterManager.printReceipt(
                    context,
                    defaultPrinter,
                    billWithItems.bill,
                    billWithItems.items,
                    kotlinx.coroutines.flow.first(prefRepo.resNameFlow),
                    kotlinx.coroutines.flow.first(prefRepo.resAddressFlow),
                    kotlinx.coroutines.flow.first(prefRepo.resPhoneFlow),
                    kotlinx.coroutines.flow.first(prefRepo.resGstFlow),
                    kotlinx.coroutines.flow.first(prefRepo.invoiceFooterFlow),
                    kotlinx.coroutines.flow.first(prefRepo.thankYouMsgFlow),
                    kotlinx.coroutines.flow.first(prefRepo.printDateFlow),
                    kotlinx.coroutines.flow.first(prefRepo.printTimeFlow),
                    kotlinx.coroutines.flow.first(prefRepo.printCashierFlow),
                    kotlinx.coroutines.flow.first(prefRepo.printPaymentMethodFlow),
                    kotlinx.coroutines.flow.first(prefRepo.printQrFlow),
                    kotlinx.coroutines.flow.first(prefRepo.printOrderTypeFlow),
                    kotlinx.coroutines.flow.first(prefRepo.printItemNotesFlow),
                    kotlinx.coroutines.flow.first(prefRepo.printGstBreakdownFlow),
                    kotlinx.coroutines.flow.first(prefRepo.printDiscountFlow),
                    kotlinx.coroutines.flow.first(prefRepo.printCustomerNameFlow),
                    kotlinx.coroutines.flow.first(prefRepo.printCustomerPhoneFlow)
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
"""

content = re.sub(
    r"    fun printBill\(context: android\.content\.Context, billWithItems: BillWithItems\) \{\n        viewModelScope\.launch \{\n            val printerDao = AppDatabase\.getDatabase\(getApplication\(\)\)\.printerDao\(\)\n            val defaultPrinter = printerDao\.getDefaultPrinterSync\(\)\n            if \(defaultPrinter != null\) \{\n                val success = com\.example\.util\.PrinterManager\.printReceipt\(\n                    context,\n                    defaultPrinter,\n                    billWithItems\.bill,\n                    billWithItems\.items\n                \)\n                billDao\.updatePrintStatus\(billWithItems\.bill\.id, if \(success\) \"PRINTED\" else \"PRINT_FAILED\"\)\n                \n                kotlinx\.coroutines\.withContext\(kotlinx\.coroutines\.Dispatchers\.Main\) \{\n                    if \(success\) \{\n                        android\.widget\.Toast\.makeText\(context, \"✓ Bill printed successfully\", android\.widget\.Toast\.LENGTH_SHORT\)\.show\(\)\n                    \} else \{\n                        android\.widget\.Toast\.makeText\(context, \"Printing Failed\", android\.widget\.Toast\.LENGTH_SHORT\)\.show\(\)\n                    \}\n                \}\n            \} else \{",
    replacement,
    content
)

content = content.replace("import com.example.data.AppDatabase", "import com.example.data.AppDatabase\nimport kotlinx.coroutines.flow.first")

with open("app/src/main/java/com/example/ui/screens/ReportsViewModel.kt", "w") as f:
    f.write(content)
