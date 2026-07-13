import re

with open("app/src/main/java/com/example/ui/screens/HomeViewModel.kt", "r") as f:
    content = f.read()

replacement = """
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
"""

content = re.sub(
    r"                val success = com\.example\.util\.PrinterManager\.printReceipt\([\s\S]*?billWithItems\.items\n                \)",
    replacement,
    content
)

content = content.replace("import com.example.data.AppDatabase", "import com.example.data.AppDatabase\nimport kotlinx.coroutines.flow.first")

with open("app/src/main/java/com/example/ui/screens/HomeViewModel.kt", "w") as f:
    f.write(content)
