# 1. HomeScreen.kt
sed -i 's/var hiddenInvoiceId by remember { mutableStateOf<Int?>(null) }//g' app/src/main/java/com/example/ui/screens/HomeScreen.kt
sed -i 's/?: recentOrders.find { it.bill.id == hiddenInvoiceId }//g' app/src/main/java/com/example/ui/screens/HomeScreen.kt
sed -i 's/onFullyClosed = { hiddenInvoiceId = null }//g' app/src/main/java/com/example/ui/screens/HomeScreen.kt
sed -i 's/hiddenInvoiceId = hiddenInvoiceId,//g' app/src/main/java/com/example/ui/screens/HomeScreen.kt
sed -i 's/hiddenInvoiceId = billWithItems.bill.id//g' app/src/main/java/com/example/ui/screens/HomeScreen.kt
sed -i 's/hiddenInvoiceId: Int? = null,//g' app/src/main/java/com/example/ui/screens/HomeScreen.kt
sed -i 's/val isSelected = billWithItems.bill.id == hiddenInvoiceId/val isSelected = false/g' app/src/main/java/com/example/ui/screens/HomeScreen.kt

# 2. TransactionHistoryScreen.kt
sed -i 's/var hiddenInvoiceId by remember { mutableStateOf<Int?>(null) }//g' app/src/main/java/com/example/ui/screens/TransactionHistoryScreen.kt
sed -i 's/?: historyBills.find { it.bill.id == hiddenInvoiceId }//g' app/src/main/java/com/example/ui/screens/TransactionHistoryScreen.kt
sed -i 's/onFullyClosed = { hiddenInvoiceId = null }//g' app/src/main/java/com/example/ui/screens/TransactionHistoryScreen.kt
sed -i 's/hiddenInvoiceId = hiddenInvoiceId,//g' app/src/main/java/com/example/ui/screens/TransactionHistoryScreen.kt
sed -i 's/hiddenInvoiceId = billWithItems.bill.id//g' app/src/main/java/com/example/ui/screens/TransactionHistoryScreen.kt

# 3. ReportsScreen.kt
sed -i 's/var hiddenInvoiceId by remember { mutableStateOf<Int?>(null) }//g' app/src/main/java/com/example/ui/screens/ReportsScreen.kt
sed -i 's/?: recentTransactions.find { it.bill.id == hiddenInvoiceId }//g' app/src/main/java/com/example/ui/screens/ReportsScreen.kt
sed -i 's/onFullyClosed = { hiddenInvoiceId = null }//g' app/src/main/java/com/example/ui/screens/ReportsScreen.kt
sed -i 's/hiddenInvoiceId = hiddenInvoiceId,//g' app/src/main/java/com/example/ui/screens/ReportsScreen.kt
sed -i 's/hiddenInvoiceId = billWithItems.bill.id//g' app/src/main/java/com/example/ui/screens/ReportsScreen.kt

