package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Bill
import com.example.data.BillItem
import com.example.data.MenuItem
import kotlinx.coroutines.flow.*
import com.example.data.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.map

data class CartItem(val menuItem: MenuItem, var quantity: Int = 1)

sealed class PrintEvent {
    object None : PrintEvent()
    object Printing : PrintEvent()
    data class Success(val billId: Int) : PrintEvent()
    data class NoPrinter(val billId: Int) : PrintEvent()
    data class Failed(val billId: Int, val message: String = "") : PrintEvent()
    data class Offline(val billId: Int) : PrintEvent()
}

class BillingViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    
    val defaultPrinter = database.printerDao().getAllPrinters().map { printers ->
        printers.firstOrNull { it.isDefault } ?: printers.firstOrNull()
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), null)


    private val repository: MenuRepository
    private val billDao: com.example.data.BillDao

    init {
        val menuDao = database.menuDao()
        billDao = database.billDao()
        repository = MenuRepository(menuDao)
    }

    private val _printEvent = MutableStateFlow<PrintEvent>(PrintEvent.None)
    val printEvent: StateFlow<PrintEvent> = _printEvent.asStateFlow()

    fun clearPrintEvent() {
        _printEvent.value = PrintEvent.None
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // For Menu Screen (all items, unfiltered by VM state)
    val allMenuItems: StateFlow<List<MenuItem>> = repository.allMenuItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = allMenuItems.map { items ->
        val cats = items.map { it.category }.distinct().filter { it.isNotBlank() }
        listOf("All") + cats
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    // For Billing (only active items, filtered)
    val activeMenuItems: StateFlow<List<MenuItem>> = combine(
        repository.activeMenuItems,
        _searchQuery,
        _selectedCategory
    ) { items, query, category ->
        items.filter {
            (category == "All" || it.category == category) &&
            (query.isEmpty() || it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addToCart(menuItem: MenuItem) {
        _cart.update { currentCart ->
            val existingItem = currentCart.find { it.menuItem.id == menuItem.id }
            if (existingItem != null) {
                currentCart.map {
                    if (it.menuItem.id == menuItem.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                currentCart + CartItem(menuItem, 1)
            }
        }
    }

    fun updateQuantity(menuItemId: Int, delta: Int) {
        _cart.update { currentCart ->
            currentCart.mapNotNull {
                if (it.menuItem.id == menuItemId) {
                    val newQuantity = it.quantity + delta
                    if (newQuantity > 0) it.copy(quantity = newQuantity) else null
                } else {
                    it
                }
            }
        }
    }

    fun clearCart() {
            
            // Auto Backup Check
            try {
                val prefs = getApplication<Application>().getSharedPreferences("auto_backup_prefs", android.content.Context.MODE_PRIVATE)
                val enabled = prefs.getBoolean("auto_backup_enabled", false)
                if (enabled) {
                    var ordersCount = prefs.getInt("orders_since_backup", 0) + 1
                    val lastBackup = prefs.getLong("last_backup_time", 0)
                    val now = System.currentTimeMillis()
                    if (ordersCount >= 50 || (now - lastBackup) > 24 * 60 * 60 * 1000L) {
                        prefs.edit()
                            .putInt("orders_since_backup", 0)
                            .putLong("last_backup_time", now)
                            .apply()
                    } else {
                        prefs.edit().putInt("orders_since_backup", ordersCount).apply()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        _cart.value = emptyList()
    }
    
    fun checkout(orderType: Int, isGstEnabled: Boolean) {
        val currentCart = _cart.value
        if (currentCart.isEmpty()) return
        
        viewModelScope.launch {
            val orderModeStr = if (orderType == 1) "Delivery" else "Restaurant"
            
            val billItems = currentCart.map {
                val effectivePrice = if (orderType == 1 && it.menuItem.deliveryPrice != null) it.menuItem.deliveryPrice else it.menuItem.price
                BillItem(
                    billId = 0,
                    menuItemId = it.menuItem.id,
                    menuItemName = it.menuItem.name,
                    menuItemCategory = it.menuItem.category,
                    quantity = it.quantity,
                    price = effectivePrice
                )
            }
            
            val subtotal = billItems.sumOf { it.price * it.quantity }
            val gst = if (isGstEnabled) subtotal * 0.05 else 0.0
            val totalAmount = subtotal + gst
            
            val totalItems = currentCart.sumOf { it.quantity }
            
            val bill = Bill(
                timestamp = System.currentTimeMillis(),
                totalAmount = totalAmount,
                totalItems = totalItems,
                orderMode = orderModeStr,
                gstAmount = gst
            )
            
            val billId = billDao.insertBill(bill)
            
            val itemsWithBillId = billItems.map { it.copy(billId = billId.toInt()) }
            
            billDao.insertBillItems(itemsWithBillId)
            clearCart()
            
            // Auto Backup Check
            try {
                val prefs = getApplication<Application>().getSharedPreferences("auto_backup_prefs", android.content.Context.MODE_PRIVATE)
                val enabled = prefs.getBoolean("auto_backup_enabled", false)
                if (enabled) {
                    var ordersCount = prefs.getInt("orders_since_backup", 0) + 1
                    val lastBackup = prefs.getLong("last_backup_time", 0)
                    val now = System.currentTimeMillis()
                    if (ordersCount >= 50 || (now - lastBackup) > 24 * 60 * 60 * 1000L) {
                        prefs.edit()
                            .putInt("orders_since_backup", 0)
                            .putLong("last_backup_time", now)
                            .apply()
                    } else {
                        prefs.edit().putInt("orders_since_backup", ordersCount).apply()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            

            val printerDao = AppDatabase.getDatabase(getApplication<Application>()).printerDao()
            val defaultPrinter = printerDao.getDefaultPrinterSync()
            
            val prefRepo = com.example.data.PrinterPreferencesRepository(getApplication<Application>())
            val printCustomerCopy = prefRepo.printCustomerCopyFlow.first()
            val printKitchenCopy = prefRepo.printKitchenCopyFlow.first()
            val openDrawer = prefRepo.openDrawerFlow.first()
            
            if (defaultPrinter != null) {
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
                }
                
                if (printKitchenCopy) {
                    com.example.util.PrinterManager.printKitchen(
                        getApplication<Application>(),
                        defaultPrinter,
                        bill.copy(id = billId.toInt()),
                        itemsWithBillId
                    )
                }
                
                if (printCustomerCopy) {
                    success = com.example.util.PrinterManager.printReceipt(
                        getApplication<Application>(), 
                        defaultPrinter, 
                        bill.copy(id = billId.toInt()), 
                        itemsWithBillId,
                        prefRepo.resNameFlow.first(),
                        prefRepo.resAddressFlow.first(),
                        prefRepo.resPhoneFlow.first(),
                        prefRepo.resGstFlow.first(),
                        prefRepo.invoiceFooterFlow.first(),
                        prefRepo.thankYouMsgFlow.first(),
                        prefRepo.printDateFlow.first(),
                        prefRepo.printTimeFlow.first(),
                        prefRepo.printCashierFlow.first(),
                        prefRepo.printPaymentMethodFlow.first(),
                        prefRepo.printQrFlow.first(),
                        prefRepo.printOrderTypeFlow.first(),
                        prefRepo.printItemNotesFlow.first(),
                        prefRepo.printGstBreakdownFlow.first(),
                        prefRepo.printDiscountFlow.first(),
                        prefRepo.printCustomerNameFlow.first(),
                        prefRepo.printCustomerPhoneFlow.first()
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

        }
    }
    
    fun saveMenuItem(item: MenuItem) {
        viewModelScope.launch {
            repository.saveMenuItem(item)
            _cart.update { currentCart ->
                currentCart.map {
                    if (it.menuItem.id == item.id) it.copy(menuItem = item) else it
                }
            }
        }
    }

        fun deleteMenuItem(id: Int, imageUrl: String) {
        viewModelScope.launch {
            repository.deleteItem(id)
            if (imageUrl.startsWith("file://")) {
                try {
                    val uri = android.net.Uri.parse(imageUrl)
                    uri.path?.let { java.io.File(it).delete() }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            _cart.update { currentCart ->
                currentCart.filter { it.menuItem.id != id }
            }
        }
    }

    
    val isImportingPdf = MutableStateFlow(false)
    val importError = MutableStateFlow<String?>(null)
    fun clearImportError() {
        importError.value = null
    }
    val pdfParsedItems = MutableStateFlow<List<MenuItem>>(emptyList())

    fun importFromPdf(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            isImportingPdf.value = true
            importError.value = null
            try {
                val items = com.example.util.PdfParser.parseMenuPdf(context, uri)
                pdfParsedItems.value = items
            } catch (e: Exception) {
                e.printStackTrace()
                importError.value = e.message ?: "An unknown error occurred during import."
            } finally {
                isImportingPdf.value = false
            }
        }
    }
    
    fun importFromImages(context: android.content.Context, uris: List<android.net.Uri>) {
        viewModelScope.launch {
            isImportingPdf.value = true
            importError.value = null
            try {
                val items = com.example.util.PdfParser.parseMenuImages(context, uris)
                pdfParsedItems.value = items
            } catch (e: Exception) {
                e.printStackTrace()
                importError.value = e.message ?: "An unknown error occurred during import."
            } finally {
                isImportingPdf.value = false
            }
        }
    }

    fun clearPdfParsedItems() {
        pdfParsedItems.value = emptyList()
    }
    
    fun updateParsedItem(index: Int, newItem: MenuItem) {
        val current = pdfParsedItems.value.toMutableList()
        if (index in current.indices) {
            current[index] = newItem
            pdfParsedItems.value = current
        }
    }

    fun updateMenuAvailability(id: Int, isActive: Boolean) {
        viewModelScope.launch {
            repository.updateAvailability(id, isActive)
        }
    }
}
