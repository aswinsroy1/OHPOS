package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import kotlinx.coroutines.flow.*
import com.example.data.BillDao
import com.example.data.BillWithItems
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val billDao: BillDao
    private val dailyClosureDao: com.example.data.DailyClosureDao

    init {
        val database = AppDatabase.getDatabase(application)
        billDao = database.billDao()
        dailyClosureDao = database.dailyClosureDao()
    }

    private val allBills = billDao.getAllBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    private val lastClosure = dailyClosureDao.getLastClosureFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayBills = combine(allBills, lastClosure) { bills, closure ->
        val lastClosedTimestamp = closure?.closedAtTimestamp ?: 0L
        bills.filter { it.bill.timestamp > lastClosedTimestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSalesToday = todayBills.map { bills ->
        bills.sumOf { it.bill.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalOrdersToday = todayBills.map { bills ->
        bills.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentOrders = allBills.map { bills ->
        bills.take(3)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    data class TopItem(val name: String, val totalQuantity: Int, val totalRevenue: Double)

    val topItems = allBills.map { bills ->
        val itemStats = mutableMapOf<String, Pair<Int, Double>>()
        for (bill in bills) {
            for (item in bill.items) {
                val current = itemStats[item.menuItemName] ?: Pair(0, 0.0)
                itemStats[item.menuItemName] = Pair(
                    current.first + item.quantity,
                    current.second + (item.price * item.quantity)
                )
            }
        }
        itemStats.map { (name, stats) ->
            TopItem(name, stats.first, stats.second)
        }.sortedByDescending { it.totalQuantity }.take(3)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
    }

    fun requestDeletion(billId: Int) {
        viewModelScope.launch {
            billDao.requestDeletion(billId)
            com.example.util.AppNotificationManager.notifyDeletionRequest(
                getApplication<Application>(),
                "Deletion request for invoice #$billId needs manager approval"
            )
        }
    }
}
