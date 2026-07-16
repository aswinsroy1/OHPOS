import re

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

bad_block = '''                        onDelete = {
                            viewModel.moveToRecycleBin(billWithItems.bill.id)
                        },
                        onPrint = {
                            viewModel.printBill(context, billWithItems)
                            toastMessage = "Bill moved to Recycle Bin"
                            showToast = true
                        }'''

good_block = '''                        onDelete = {
                            viewModel.moveToRecycleBin(billWithItems.bill.id)
                            toastMessage = "Bill moved to Recycle Bin"
                            showToast = true
                        },
                        onPrint = {
                            viewModel.printBill(context, billWithItems)
                        }'''

content = content.replace(bad_block, good_block)

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)
