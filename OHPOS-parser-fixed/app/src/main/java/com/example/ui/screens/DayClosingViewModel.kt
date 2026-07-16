package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DailyClosure
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

data class DayStats(
    val restaurantName: String = "",
    val businessDate: String = "",
    val orders: Int = 0,
    val grossSales: Double = 0.0,
    val gstCollected: Double = 0.0,
    val discounts: Double = 0.0,
    val cash: Double = 0.0,
    val upi: Double = 0.0,
    val card: Double = 0.0,
    val averageBill: Double = 0.0,
    val highestBill: Double = 0.0,
    val lowestBill: Double = 0.0,
    val topSellingItem: String = "-",
    val topSellingCategory: String = "-",
    val itemsSold: Int = 0,
    val openingTime: String = "-",
    val currentTime: String = "-"
)

class DayClosingViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val closureDao = db.dailyClosureDao()
    private val billDao = db.billDao()
    private val prefRepo = com.example.data.PrinterPreferencesRepository(application)
    
    val lastClosure = closureDao.getLastClosureFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        
    val isDayClosed = lastClosure.map { closure ->
        val calendar = Calendar.getInstance()
        val todayStr = "${calendar.get(Calendar.YEAR)}-${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}-${String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))}"
        closure?.dateString == todayStr
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val dayStats = combine(
        billDao.getAllBills(),
        lastClosure,
        prefRepo.resNameFlow
    ) { bills, closure, resName ->
        val lastClosedTimestamp = closure?.closedAtTimestamp ?: 0L
        val todayBills = bills.filter { it.bill.timestamp > lastClosedTimestamp }
        
        val calendar = Calendar.getInstance()
        val todayStr = "${calendar.get(Calendar.YEAR)}-${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}-${String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))}"
        val currTimeStr = "${String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY))}:${String.format("%02d", calendar.get(Calendar.MINUTE))}"
        
        var openingTime = "-"
        if (todayBills.isNotEmpty()) {
            val firstBill = todayBills.last().bill
            val c = Calendar.getInstance()
            c.timeInMillis = firstBill.timestamp
            openingTime = "${String.format("%02d", c.get(Calendar.HOUR_OF_DAY))}:${String.format("%02d", c.get(Calendar.MINUTE))}"
        }
        
        val totalSales = todayBills.sumOf { it.bill.totalAmount }
        
        var topItemName = "-"
        var topCatName = "-"
        
        val itemStats = mutableMapOf<String, Int>()
        val catStats = mutableMapOf<String, Int>()
        
        for (bill in todayBills) {
            for (item in bill.items) {
                itemStats[item.menuItemName] = (itemStats[item.menuItemName] ?: 0) + item.quantity
                catStats[item.menuItemCategory] = (catStats[item.menuItemCategory] ?: 0) + item.quantity
            }
        }
        if (itemStats.isNotEmpty()) topItemName = itemStats.maxByOrNull { it.value }?.key ?: "-"
        if (catStats.isNotEmpty()) topCatName = catStats.maxByOrNull { it.value }?.key ?: "-"
        
        DayStats(
            restaurantName = resName,
            businessDate = todayStr,
            orders = todayBills.size,
            grossSales = totalSales,
            gstCollected = todayBills.sumOf { it.bill.gstAmount },
            discounts = todayBills.sumOf { it.bill.discountAmount },
            cash = todayBills.filter { it.bill.paymentMethod == "CASH" }.sumOf { it.bill.totalAmount },
            upi = todayBills.filter { it.bill.paymentMethod == "UPI" }.sumOf { it.bill.totalAmount },
            card = todayBills.filter { it.bill.paymentMethod == "CARD" }.sumOf { it.bill.totalAmount },
            averageBill = if (todayBills.isNotEmpty()) totalSales / todayBills.size else 0.0,
            highestBill = todayBills.maxOfOrNull { it.bill.totalAmount } ?: 0.0,
            lowestBill = todayBills.minOfOrNull { it.bill.totalAmount } ?: 0.0,
            topSellingItem = topItemName,
            topSellingCategory = topCatName,
            itemsSold = todayBills.sumOf { it.bill.totalItems },
            openingTime = openingTime,
            currentTime = currTimeStr
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DayStats())
    
    val todayBillsFlow = combine(
        billDao.getAllBills(),
        lastClosure
    ) { bills, closure ->
        val lastClosedTimestamp = closure?.closedAtTimestamp ?: 0L
        bills.filter { it.bill.timestamp > lastClosedTimestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Wait for the app to reach a stable state before starting automatic checks
                kotlinx.coroutines.delay(5000)
                
                dayStats.collect {
                    try {
                        checkAutomaticRollover(getApplication<Application>().applicationContext)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Wait for the app to reach a stable state
                kotlinx.coroutines.delay(5000)
                
                while (true) {
                    try {
                        checkAutomaticRollover(getApplication<Application>().applicationContext)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    kotlinx.coroutines.delay(60000)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun checkAutomaticRollover(context: android.content.Context) {
        try {
            val sharedPrefs = context.getSharedPreferences("ohpos_prefs", android.content.Context.MODE_PRIVATE)
            val savedFolderUriStr = sharedPrefs.getString("report_folder_uri", null)
            val lastActiveDate = sharedPrefs.getString("last_active_business_date", null)
            
            val calendar = Calendar.getInstance()
            val todayStr = "${calendar.get(Calendar.YEAR)}-${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}-${String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))}"
            
            if (lastActiveDate == null) {
                sharedPrefs.edit().putString("last_active_business_date", todayStr).apply()
                return
            }
            
            if (lastActiveDate != todayStr) {
                val lastClosureObj = closureDao.getLastClosureFlow().first()
                if (lastClosureObj?.dateString != lastActiveDate) {
                    val bills = billDao.getAllBills().first()
                    val lastClosedTimestamp = lastClosureObj?.closedAtTimestamp ?: 0L
                    val unclosedBills = bills.filter { it.bill.timestamp > lastClosedTimestamp }
                    
                    if (unclosedBills.isNotEmpty() && savedFolderUriStr != null) {
                        val resName = prefRepo.resNameFlow.first()
                        
                        var openingTime = "-"
                        val firstBill = unclosedBills.last().bill
                        val c = Calendar.getInstance()
                        c.timeInMillis = firstBill.timestamp
                        openingTime = "${String.format("%02d", c.get(Calendar.HOUR_OF_DAY))}:${String.format("%02d", c.get(Calendar.MINUTE))}"
                        
                        val totalSales = unclosedBills.sumOf { it.bill.totalAmount }
                        
                        var topItemName = "-"
                        var topCatName = "-"
                        val itemStats = mutableMapOf<String, Int>()
                        val catStats = mutableMapOf<String, Int>()
                        for (bill in unclosedBills) {
                            for (item in bill.items) {
                                itemStats[item.menuItemName] = (itemStats[item.menuItemName] ?: 0) + item.quantity
                                catStats[item.menuItemCategory] = (catStats[item.menuItemCategory] ?: 0) + item.quantity
                            }
                        }
                        if (itemStats.isNotEmpty()) topItemName = itemStats.maxByOrNull { it.value }?.key ?: "-"
                        if (catStats.isNotEmpty()) topCatName = catStats.maxByOrNull { it.value }?.key ?: "-"
                        
                        val currTimeStr = "23:59"
                        
                        val autoStats = DayStats(
                            restaurantName = resName,
                            businessDate = lastActiveDate,
                            orders = unclosedBills.size,
                            grossSales = totalSales,
                            gstCollected = unclosedBills.sumOf { it.bill.gstAmount },
                            discounts = unclosedBills.sumOf { it.bill.discountAmount },
                            cash = unclosedBills.filter { it.bill.paymentMethod == "CASH" }.sumOf { it.bill.totalAmount },
                            upi = unclosedBills.filter { it.bill.paymentMethod == "UPI" }.sumOf { it.bill.totalAmount },
                            card = unclosedBills.filter { it.bill.paymentMethod == "CARD" }.sumOf { it.bill.totalAmount },
                            averageBill = if (unclosedBills.isNotEmpty()) totalSales / unclosedBills.size else 0.0,
                            highestBill = unclosedBills.maxOfOrNull { it.bill.totalAmount } ?: 0.0,
                            lowestBill = unclosedBills.minOfOrNull { it.bill.totalAmount } ?: 0.0,
                            topSellingItem = topItemName,
                            topSellingCategory = topCatName,
                            itemsSold = unclosedBills.sumOf { it.bill.totalItems },
                            openingTime = openingTime,
                            currentTime = currTimeStr
                        )
                        
                        try {
                            val uriStr = com.example.util.PdfGenerator.generateDailyReport(context, android.net.Uri.parse(savedFolderUriStr), autoStats, unclosedBills)
                            if (uriStr != null) {
                                val closure = DailyClosure(
                                    dateString = lastActiveDate,
                                    closedAtTimestamp = System.currentTimeMillis(),
                                    totalOrders = autoStats.orders,
                                    totalSales = autoStats.grossSales,
                                    pdfFilePath = uriStr
                                )
                                closureDao.insertClosure(closure)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else if (unclosedBills.isEmpty()) {
                        val closure = DailyClosure(
                            dateString = lastActiveDate,
                            closedAtTimestamp = System.currentTimeMillis(),
                            totalOrders = 0,
                            totalSales = 0.0,
                            pdfFilePath = ""
                        )
                        closureDao.insertClosure(closure)
                    }
                }
                sharedPrefs.edit().putString("last_active_business_date", todayStr).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun closeDay(context: android.content.Context, folderUri: android.net.Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val stats = dayStats.value
            val bills = todayBillsFlow.value
            
            val uriStr = com.example.util.PdfGenerator.generateDailyReport(context, folderUri, stats, bills)
            if (uriStr != null) {
                val closure = DailyClosure(
                    dateString = stats.businessDate,
                    closedAtTimestamp = System.currentTimeMillis(),
                    totalOrders = stats.orders,
                    totalSales = stats.grossSales,
                    pdfFilePath = uriStr
                )
                closureDao.insertClosure(closure)
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onSuccess()
                }
            } else {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError()
                }
            }
        }
    }
}
