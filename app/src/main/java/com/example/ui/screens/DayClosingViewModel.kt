package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DailyClosure
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    private val dailyClosingPrefRepo = com.example.data.DailyClosingPreferencesRepository(application)
    private val rolloverMutex = Mutex()
    
    private val _autoCloseFailed = MutableStateFlow(false)
    val autoCloseFailed = _autoCloseFailed.asStateFlow()
    
    fun clearAutoCloseFailure() {
        _autoCloseFailed.value = false
    }
    
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

    private suspend fun checkAutomaticRollover(context: android.content.Context) = rolloverMutex.withLock {
        try {
            val isAutoCloseEnabled = dailyClosingPrefRepo.isAutoCloseEnabled.first()
            if (!isAutoCloseEnabled) return@withLock
            
            val autoCloseHour = dailyClosingPrefRepo.autoCloseHour.first()
            val autoCloseMinute = dailyClosingPrefRepo.autoCloseMinute.first()
            val savedFolderUriStr = dailyClosingPrefRepo.exportFolderUri.first()
            
            val sharedPrefs = context.getSharedPreferences("ohpos_prefs", android.content.Context.MODE_PRIVATE)
            
            val calendar = Calendar.getInstance()
            val todayStr = "${calendar.get(Calendar.YEAR)}-${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}-${String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))}"
            
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            val hasPassedAutoCloseTime = (currentHour > autoCloseHour) || (currentHour == autoCloseHour && currentMinute >= autoCloseMinute)
            
            val lastClosureObj = closureDao.getLastClosureFlow().first()
            var startDateStr: String? = null
            
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            
            if (lastClosureObj != null) {
                val lastDateParsed = dateFormat.parse(lastClosureObj.dateString)
                if (lastDateParsed != null) {
                    val cal = Calendar.getInstance().apply { time = lastDateParsed }
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                    startDateStr = dateFormat.format(cal.time)
                }
            } else {
                startDateStr = sharedPrefs.getString("last_active_business_date", null)
                if (startDateStr == null) {
                    sharedPrefs.edit().putString("last_active_business_date", todayStr).apply()
                    return@withLock
                }
            }
            
            if (startDateStr == null) return@withLock
            
            val startParsed = dateFormat.parse(startDateStr) ?: return@withLock
            val todayParsed = dateFormat.parse(todayStr) ?: return@withLock
            
            val datesToClose = mutableListOf<String>()
            val loopCalendar = Calendar.getInstance().apply { time = startParsed }
            
            while (loopCalendar.timeInMillis <= todayParsed.time) {
                val loopDateStr = dateFormat.format(loopCalendar.time)
                val isToday = loopDateStr == todayStr
                
                val shouldCloseThisDay = if (isToday) {
                    hasPassedAutoCloseTime
                } else {
                    true
                }
                
                if (shouldCloseThisDay) {
                    datesToClose.add(loopDateStr)
                }
                
                loopCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            val bills = billDao.getAllBills().first()
            
            for (dateToCloseStr in datesToClose) {
                val existingClosure = closureDao.getClosureForDate(dateToCloseStr)
                if (existingClosure != null) continue
                
                val parsedDate = dateFormat.parse(dateToCloseStr)!!
                val dayStartCal = Calendar.getInstance().apply {
                    time = parsedDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val dayStart = dayStartCal.timeInMillis
                
                val dayEndCal = Calendar.getInstance().apply {
                    time = parsedDate
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val dayEnd = dayEndCal.timeInMillis
                
                val dayBills = bills.filter { it.bill.timestamp in dayStart..dayEnd }
                
                var pdfUriStr: String? = "" 
                
                if (dayBills.isNotEmpty() && savedFolderUriStr != null) {
                    val resName = prefRepo.resNameFlow.first()
                    
                    var openingTime = "-"
                    val firstBill = dayBills.first().bill
                    val c = Calendar.getInstance()
                    c.timeInMillis = firstBill.timestamp
                    openingTime = "${String.format("%02d", c.get(Calendar.HOUR_OF_DAY))}:${String.format("%02d", c.get(Calendar.MINUTE))}"
                    
                    val totalSales = dayBills.sumOf { it.bill.totalAmount }
                    
                    var topItemName = "-"
                    var topCatName = "-"
                    val itemStats = mutableMapOf<String, Int>()
                    val catStats = mutableMapOf<String, Int>()
                    for (bill in dayBills) {
                        for (item in bill.items) {
                            itemStats[item.menuItemName] = (itemStats[item.menuItemName] ?: 0) + item.quantity
                            catStats[item.menuItemCategory] = (catStats[item.menuItemCategory] ?: 0) + item.quantity
                        }
                    }
                    if (itemStats.isNotEmpty()) topItemName = itemStats.maxByOrNull { it.value }?.key ?: "-"
                    if (catStats.isNotEmpty()) topCatName = catStats.maxByOrNull { it.value }?.key ?: "-"
                    
                    val currTimeStr = String.format("%02d:%02d", autoCloseHour, autoCloseMinute)
                    
                    val autoStats = DayStats(
                        restaurantName = resName,
                        businessDate = dateToCloseStr,
                        orders = dayBills.size,
                        grossSales = totalSales,
                        gstCollected = dayBills.sumOf { it.bill.gstAmount },
                        discounts = dayBills.sumOf { it.bill.discountAmount },
                        cash = dayBills.filter { it.bill.paymentMethod == "CASH" }.sumOf { it.bill.totalAmount },
                        upi = dayBills.filter { it.bill.paymentMethod == "UPI" }.sumOf { it.bill.totalAmount },
                        card = dayBills.filter { it.bill.paymentMethod == "CARD" }.sumOf { it.bill.totalAmount },
                        averageBill = if (dayBills.isNotEmpty()) totalSales / dayBills.size else 0.0,
                        highestBill = dayBills.maxOfOrNull { it.bill.totalAmount } ?: 0.0,
                        lowestBill = dayBills.minOfOrNull { it.bill.totalAmount } ?: 0.0,
                        topSellingItem = topItemName,
                        topSellingCategory = topCatName,
                        itemsSold = dayBills.sumOf { it.bill.totalItems },
                        openingTime = openingTime,
                        currentTime = currTimeStr
                    )
                    
                    try {
                        val genUriStr = com.example.util.PdfGenerator.generateDailyReport(context, android.net.Uri.parse(savedFolderUriStr), autoStats, dayBills)
                        if (genUriStr != null) {
                            pdfUriStr = genUriStr
                        } else {
                            _autoCloseFailed.value = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _autoCloseFailed.value = true
                    }
                } else if (dayBills.isNotEmpty() && savedFolderUriStr == null) {
                    _autoCloseFailed.value = true
                }
                
                val closure = DailyClosure(
                    dateString = dateToCloseStr,
                    closedAtTimestamp = System.currentTimeMillis(),
                    totalOrders = dayBills.size,
                    totalSales = dayBills.sumOf { it.bill.totalAmount },
                    pdfFilePath = pdfUriStr ?: ""
                )
                closureDao.insertClosure(closure)
                
                com.example.util.AppNotificationManager.notifyDayClosed(
                    context,
                    "Business day for $dateToCloseStr has been automatically closed.",
                    pdfUriStr
                )
            }
            
            sharedPrefs.edit().putString("last_active_business_date", todayStr).apply()
            
        } catch (e: Exception) {
            e.printStackTrace()
            _autoCloseFailed.value = true
        }
    }

    fun closeDay(context: android.content.Context, folderUri: android.net.Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val stats = dayStats.value
            
            val existing = closureDao.getClosureForDate(stats.businessDate)
            if (existing != null) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError("This day has already been closed.")
                }
                return@launch
            }
            
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
                    onError("Failed to generate PDF")
                }
            }
        }
    }
}
