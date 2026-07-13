package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import kotlinx.coroutines.flow.*
import com.example.data.BillDao
import com.example.data.BillWithItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class ReportsViewModel(application: Application) : AndroidViewModel(application) {
    private val billDao: BillDao

    init {
        val database = AppDatabase.getDatabase(application)
        billDao = database.billDao()
    }

    private val dailyClosureDao: com.example.data.DailyClosureDao
    init {
        dailyClosureDao = AppDatabase.getDatabase(application).dailyClosureDao()
    }
    
    val allBills = billDao.getAllBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    private val lastClosure = dailyClosureDao.getLastClosureFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        
    val archivedReports = dailyClosureDao.getAllClosures()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTimeFilter = MutableStateFlow("Today")
    val selectedTimeFilter: StateFlow<String> = _selectedTimeFilter.asStateFlow()

    fun setTimeFilter(filter: String) {
        _selectedTimeFilter.value = filter
    }
    
    val filteredBills = combine(allBills, _selectedTimeFilter, lastClosure) { bills, filter, closure ->
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now
        
        when (filter) {
            "Today" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                bills.filter { it.bill.timestamp >= startOfDay }
            }
            "This Week" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfWeek = calendar.timeInMillis
                bills.filter { it.bill.timestamp >= startOfWeek }
            }
            "This Month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.timeInMillis
                bills.filter { it.bill.timestamp >= startOfMonth }
            }
            else -> bills
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions = allBills.map { bills ->
        bills.take(4)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    private val _historyTimeFilter = MutableStateFlow("Today")
    val historyTimeFilter: StateFlow<String> = _historyTimeFilter.asStateFlow()

    fun setHistoryTimeFilter(filter: String) {
        _historyTimeFilter.value = filter
    }

    val filteredHistoryBills = combine(allBills, _historySearchQuery, _historyTimeFilter, lastClosure) { bills, query, filter, closure ->
        var result = bills

        if (query.isNotBlank()) {
            result = result.filter { 
                it.bill.id.toString().contains(query, ignoreCase = true)
            }
        }

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now

        result = when (filter) {
            "Today" -> {
                val lastClosedTimestamp = closure?.closedAtTimestamp ?: 0L
                result.filter { it.bill.timestamp > lastClosedTimestamp }
            }
            "Yesterday" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val startOfYesterday = calendar.timeInMillis
                
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val endOfYesterday = calendar.timeInMillis
                
                result.filter { it.bill.timestamp in startOfYesterday until endOfYesterday }
            }
            "This Week" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfWeek = calendar.timeInMillis
                result.filter { it.bill.timestamp >= startOfWeek }
            }
            "This Month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.timeInMillis
                result.filter { it.bill.timestamp >= startOfMonth }
            }
            else -> result // "All Time" or "Custom Range" for now we will just show all if Custom Range isn't fully handled yet
        }

        result.sortedByDescending { it.bill.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSales = combine(filteredBills) { billsArray ->
        val bills = billsArray.first()
        bills.sumOf { it.bill.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalOrders = combine(filteredBills) { billsArray ->
        val bills = billsArray.first()
        bills.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    data class CategoryStat(val name: String, val percentage: Float, val revenue: Double)

    val topCategories = combine(filteredBills) { billsArray ->
        val bills = billsArray.first()
        val categoryRevenue = mutableMapOf<String, Double>()
        var totalRevenue = 0.0

        for (billWithItems in bills) {
            for (item in billWithItems.items) {
                val revenue = item.price * item.quantity
                categoryRevenue[item.menuItemCategory] = (categoryRevenue[item.menuItemCategory] ?: 0.0) + revenue
                totalRevenue += revenue
            }
        }
        
        if (totalRevenue == 0.0) return@combine emptyList<CategoryStat>()

        categoryRevenue.map { (category, revenue) ->
            CategoryStat(category, (revenue / totalRevenue).toFloat(), revenue)
        }.sortedByDescending { it.revenue }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    data class ChartData(val label: String, val sales: Double, val orders: Int)
    
    val salesChartData = combine(filteredBills, _selectedTimeFilter) { bills, filter ->
        val result = mutableListOf<ChartData>()
        val calendar = Calendar.getInstance()
        
        when (filter) {
            "Today" -> {
                // Group by hour
                val grouped = bills.groupBy { 
                    calendar.timeInMillis = it.bill.timestamp
                    calendar.get(Calendar.HOUR_OF_DAY)
                }
                for (hour in 8..22 step 2) { // 8 AM to 10 PM
                    val hourBills = grouped[hour] ?: emptyList()
                    val nextHourBills = grouped[hour + 1] ?: emptyList()
                    val combined = hourBills + nextHourBills
                    result.add(
                        ChartData(
                            label = "${if (hour > 12) hour - 12 else if (hour == 0) 12 else hour}${if (hour >= 12) "PM" else "AM"}",
                            sales = combined.sumOf { it.bill.totalAmount },
                            orders = combined.size
                        )
                    )
                }
            }
            "This Week" -> {
                // Group by day of week
                val grouped = bills.groupBy {
                    calendar.timeInMillis = it.bill.timestamp
                    calendar.get(Calendar.DAY_OF_WEEK)
                }
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                for (i in 0..6) {
                    val dayOfWeek = if (i == 6) Calendar.SUNDAY else i + 2
                    val dayBills = grouped[dayOfWeek] ?: emptyList()
                    result.add(
                        ChartData(
                            label = days[i],
                            sales = dayBills.sumOf { it.bill.totalAmount },
                            orders = dayBills.size
                        )
                    )
                }
            }
            "This Month" -> {
                // Group by week
                val grouped = bills.groupBy {
                    calendar.timeInMillis = it.bill.timestamp
                    calendar.get(Calendar.WEEK_OF_MONTH)
                }
                for (week in 1..5) {
                    val weekBills = grouped[week] ?: emptyList()
                    result.add(
                        ChartData(
                            label = "W$week",
                            sales = weekBills.sumOf { it.bill.totalAmount },
                            orders = weekBills.size
                        )
                    )
                }
            }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun requestDeletion(billId: Int) {
        viewModelScope.launch {
            billDao.requestDeletion(billId)
        }
    }


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

    suspend fun generateCustomReport(
        context: android.content.Context,
        outStream: java.io.OutputStream,
        timeFilter: String,
        resName: String
    ): Boolean {
        val allBillsList = allBills.value
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = now

        val targetBills = when (timeFilter) {
            "Today's Report", "Today" -> {
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                allBillsList.filter { it.bill.timestamp >= startOfDay }
            }
            "Last 7 Days" -> {
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -7)
                val start = calendar.timeInMillis
                allBillsList.filter { it.bill.timestamp >= start }
            }
            "Last 30 Days" -> {
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -30)
                val start = calendar.timeInMillis
                allBillsList.filter { it.bill.timestamp >= start }
            }
            else -> allBillsList // Custom Date Range will just use all for now unless we add a UI picker
        }

        if (targetBills.isEmpty()) {
            return false
        }
        val totalSales = targetBills.sumOf { it.bill.totalAmount }
        var topItemName = "-"
        var topCatName = "-"
        val itemStats = mutableMapOf<String, Int>()
        val catStats = mutableMapOf<String, Int>()

        for (bill in targetBills) {
            for (item in bill.items) {
                itemStats[item.menuItemName] = (itemStats[item.menuItemName] ?: 0) + item.quantity
                catStats[item.menuItemCategory] = (catStats[item.menuItemCategory] ?: 0) + item.quantity
            }
        }
        if (itemStats.isNotEmpty()) topItemName = itemStats.maxByOrNull { it.value }?.key ?: "-"
        if (catStats.isNotEmpty()) topCatName = catStats.maxByOrNull { it.value }?.key ?: "-"

        val stats = com.example.ui.screens.DayStats(
            restaurantName = resName,
            businessDate = timeFilter,
            orders = targetBills.size,
            grossSales = totalSales,
            gstCollected = targetBills.sumOf { it.bill.gstAmount },
            discounts = targetBills.sumOf { it.bill.discountAmount },
            cash = targetBills.filter { it.bill.paymentMethod == "CASH" }.sumOf { it.bill.totalAmount },
            upi = targetBills.filter { it.bill.paymentMethod == "UPI" }.sumOf { it.bill.totalAmount },
            card = targetBills.filter { it.bill.paymentMethod == "CARD" }.sumOf { it.bill.totalAmount },
            averageBill = if (targetBills.isNotEmpty()) totalSales / targetBills.size else 0.0,
            highestBill = targetBills.maxOfOrNull { it.bill.totalAmount } ?: 0.0,
            lowestBill = targetBills.minOfOrNull { it.bill.totalAmount } ?: 0.0,
            topSellingItem = topItemName,
            topSellingCategory = topCatName,
            itemsSold = targetBills.sumOf { it.bill.totalItems },
            openingTime = "-",
            currentTime = "-"
        )

        return com.example.util.PdfGenerator.generateReportToStream(context, outStream, stats, targetBills)
    }

}