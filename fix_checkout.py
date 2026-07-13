import sys
import re

with open("app/src/main/java/com/example/ui/screens/BillingViewModel.kt", "r") as f:
    content = f.read()

replacement = """
            val printerDao = AppDatabase.getDatabase(getApplication()).printerDao()
            val defaultPrinter = printerDao.getDefaultPrinterSync()
            
            val prefRepo = com.example.data.PrinterPreferencesRepository(getApplication())
            val printCustomerCopy = kotlinx.coroutines.flow.first(prefRepo.printCustomerCopyFlow)
            val printKitchenCopy = kotlinx.coroutines.flow.first(prefRepo.printKitchenCopyFlow)
            val openDrawer = kotlinx.coroutines.flow.first(prefRepo.openDrawerFlow)
            
            if (defaultPrinter != null) {
                var success = true
                if (openDrawer) {
                    com.example.util.PrinterManager.openDrawer(defaultPrinter)
                }
                
                if (printKitchenCopy) {
                    com.example.util.PrinterManager.printKitchen(
                        getApplication(),
                        defaultPrinter,
                        bill.copy(id = billId.toInt()),
                        itemsWithBillId
                    )
                }
                
                if (printCustomerCopy) {
                    success = com.example.util.PrinterManager.printReceipt(
                        getApplication(), 
                        defaultPrinter, 
                        bill.copy(id = billId.toInt()), 
                        itemsWithBillId,
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
                }

                billDao.updatePrintStatus(billId.toInt(), if (success) "PRINTED" else "PRINT_FAILED")
                
                if (success) {
                    _printEvent.value = PrintEvent.Success(billId.toInt())
                } else {
                    _printEvent.value = PrintEvent.Failed(billId.toInt())
                }
            } else {
                billDao.updatePrintStatus(billId.toInt(), "SAVED")
                _printEvent.value = PrintEvent.NoPrinter(billId.toInt())
            }
"""

content = re.sub(r"            val printerDao = AppDatabase\.getDatabase\(getApplication\(\)\)\.printerDao\(\)[\s\S]*?_printEvent\.value = PrintEvent\.NoPrinter\(billId\.toInt\(\)\)\n            \}", replacement, content)

content = content.replace("import com.example.data.MenuItem", "import com.example.data.MenuItem\nimport kotlinx.coroutines.flow.first")

with open("app/src/main/java/com/example/ui/screens/BillingViewModel.kt", "w") as f:
    f.write(content)
