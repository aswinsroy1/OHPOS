package com.example.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.hardware.usb.UsbManager
import com.example.data.AppDatabase
import com.example.data.SavedPrinter
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.InetSocketAddress
import java.net.Socket
import java.util.UUID

enum class PrinterState {
    CONNECTED, CONNECTING, STANDBY, OFFLINE, DISCONNECTED;

    fun getDisplayName(): String = when (this) {
        CONNECTED -> "Connected"
        CONNECTING -> "Connecting..."
        STANDBY -> "Standby"
        OFFLINE -> "Offline"
        DISCONNECTED -> "Disconnected"
    }
}

data class PrinterStatus(
    val state: PrinterState,
    val lastCheckTime: Long = System.currentTimeMillis()
)

@SuppressLint("MissingPermission")
object PrinterStatusMonitor {
    private val _statuses = MutableStateFlow<Map<Int, PrinterStatus>>(emptyMap())
    val statuses: StateFlow<Map<Int, PrinterStatus>> = _statuses.asStateFlow()

    private var monitorJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun startMonitoring(context: Context, database: AppDatabase) {
        if (monitorJob != null) return
        monitorJob = scope.launch {
            database.printerDao().getAllPrinters().collect { printers ->
                while (isActive) {
                    val currentStatuses = _statuses.value.toMutableMap()
                    
                    for (printer in printers) {
                        if (!currentStatuses.containsKey(printer.id)) {
                            currentStatuses[printer.id] = PrinterStatus(PrinterState.CONNECTING)
                            _statuses.value = currentStatuses.toMap()
                        }
                        
                        val isOnline = checkPrinterStatus(context, printer)
                        
                        currentStatuses[printer.id] = if (isOnline) {
                            PrinterStatus(PrinterState.CONNECTED)
                        } else {
                            if (printer.type == "USB") {
                                val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
                                val device = usbManager.deviceList.values.find { it.deviceName == printer.address || "${it.vendorId}:${it.productId}" == printer.address }
                                if (device != null) PrinterStatus(PrinterState.OFFLINE) // Permission revoked
                                else PrinterStatus(PrinterState.DISCONNECTED) // Cable removed
                            } else {
                                PrinterStatus(PrinterState.OFFLINE)
                            }
                        }
                    }
                    _statuses.value = currentStatuses.toMap()
                    
                    delay(5000) // Poll every 5 seconds
                }
            }
        }
    }

    private suspend fun checkPrinterStatus(context: Context, printer: SavedPrinter): Boolean = withContext(Dispatchers.IO) {
        try {
            when (printer.type) {
                "WIFI" -> {
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(printer.address, 9100), 2000)
                        socket.close()
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
                "USB" -> {
                    val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
                    val deviceList = usbManager.deviceList
                    val device = deviceList.values.find { it.deviceName == printer.address || "${it.vendorId}:${it.productId}" == printer.address }
                    if (device != null) {
                        usbManager.hasPermission(device)
                    } else {
                        false
                    }
                }
                "BLUETOOTH" -> {
                    val adapter = BluetoothAdapter.getDefaultAdapter()
                    if (adapter == null || !adapter.isEnabled) return@withContext false
                    val device = adapter.bondedDevices?.find { it.address == printer.address }
                    if (device != null) {
                        try {
                            // Instead of actually connecting which takes a long time and makes the printer beep,
                            // we just consider bonded printers as Standby, wait, the user wanted:
                            // "Bluetooth: Connected, Disconnected, Connecting".
                            // For a realistic continuous check without beep spam, checking bond state is the safest fallback.
                            // But let's attempt a quick connection to be sure, maybe with a timeout?
                            // Actually, let's just assume bonded is Connected for now if we can't reliably ping without disruption.
                            // Let's do a fast RFCOMM ping.
                            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                            val socket = device.createRfcommSocketToServiceRecord(uuid)
                            socket.connect()
                            socket.close()
                            true
                        } catch (e: Exception) {
                            false
                        }
                    } else {
                        false
                    }
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
}
