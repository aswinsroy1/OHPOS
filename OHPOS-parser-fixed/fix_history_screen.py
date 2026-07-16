import re

with open('app/src/main/java/com/example/ui/screens/TransactionHistoryScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('val selectedInvoice = historyBills.find { it.bill.id == selectedInvoiceId }', 
    'val selectedInvoice = historyBills.find { it.bill.id == selectedInvoiceId }\n    val context = androidx.compose.ui.platform.LocalContext.current')

old_block = '''                        onDelete = {
                            viewModel.moveToRecycleBin(billWithItems.bill.id)
                        }'''

new_block = '''                        onDelete = {
                            viewModel.moveToRecycleBin(billWithItems.bill.id)
                        },
                        onPrint = {
                            viewModel.printBill(context, billWithItems)
                        }'''

content = content.replace(old_block, new_block)

with open('app/src/main/java/com/example/ui/screens/TransactionHistoryScreen.kt', 'w') as f:
    f.write(content)
