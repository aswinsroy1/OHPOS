sed -i 's/var selectedInvoiceId by remember { mutableStateOf<Int?>(null) }/var selectedInvoiceId by remember { mutableStateOf<Int?>(null) }\n    var hiddenInvoiceId by remember { mutableStateOf<Int?>(null) }/g' app/src/main/java/com/example/ui/screens/TransactionHistoryScreen.kt
sed -i 's/val selectedInvoice = historyBills.find { it.bill.id == selectedInvoiceId }/val selectedInvoice = historyBills.find { it.bill.id == selectedInvoiceId } ?: historyBills.find { it.bill.id == hiddenInvoiceId }/g' app/src/main/java/com/example/ui/screens/TransactionHistoryScreen.kt

sed -i 's/onDismissRequest = { selectedInvoiceId = null }/onDismissRequest = { selectedInvoiceId = null },\n        onFullyClosed = { hiddenInvoiceId = null }/g' app/src/main/java/com/example/ui/screens/TransactionHistoryScreen.kt

sed -i 's/selectedInvoiceId = selectedInvoiceId,/hiddenInvoiceId = hiddenInvoiceId,/g' app/src/main/java/com/example/ui/screens/TransactionHistoryScreen.kt
sed -i 's/selectedInvoiceId = billWithItems.bill.id/hiddenInvoiceId = billWithItems.bill.id\n                                selectedInvoiceId = billWithItems.bill.id/g' app/src/main/java/com/example/ui/screens/TransactionHistoryScreen.kt
