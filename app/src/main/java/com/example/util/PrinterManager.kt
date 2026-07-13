package com.example.util

import android.content.Context
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.connection.tcp.TcpConnection
import com.example.data.Bill
import com.example.data.BillItem
import com.example.data.SavedPrinter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SimpleDeviceInfo(val name: String, val address: String)

object PrinterManager {
    @android.annotation.SuppressLint("MissingPermission")
    fun getPairedBluetoothPrinters(context: Context): List<SimpleDeviceInfo> {
        val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
        return adapter?.bondedDevices?.map { SimpleDeviceInfo(it.name ?: "Unknown", it.address) } ?: emptyList()
    }

    private val printMutex = Mutex()

    private fun getConnection(printer: SavedPrinter): DeviceConnection {
        return when (printer.type) {
            "WIFI", "Network / Wi-Fi" -> {
                val ip = printer.address.split(":").getOrNull(0) ?: printer.address
                val port = printer.address.split(":").getOrNull(1)?.toIntOrNull() ?: 9100
                TcpConnection(ip, port)
            }
                        "BLUETOOTH", "Bluetooth" -> {
                val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                @android.annotation.SuppressLint("MissingPermission")
                val device = adapter?.bondedDevices?.firstOrNull { it.address == printer.address }
                device ?: throw Exception("Bluetooth printer not found")
                com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection(device)
            }
            else -> throw Exception("Unsupported printer type: ${printer.type}")
        }
    }

    suspend fun printTest(context: Context, printer: SavedPrinter): Boolean = withContext(Dispatchers.IO) {
        printMutex.withLock {
            try {
                val connection = getConnection(printer)
                connection.connect()
                val prefRepo = com.example.data.PrinterPreferencesRepository(context)
                val paperSize = prefRepo.paperSizeFlow.first()
                val charsPerLine = if (paperSize == 80) 48 else 32
                val escPos = EscPosPrinter(connection, 203, paperSize.toFloat(), charsPerLine)
                
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = Date(System.currentTimeMillis())
                escPos.printFormattedText(
                    "[C]" + "-".repeat(charsPerLine) + "\n" +
                    "[C]<u><font size='big'>OH POS</font></u>\n" +
                    "[C]Printer Test\n" +
                    "[C]\n" +
                    "[C]Printer Connected Successfully\n" +
                    "[C]\n" +
                    "[C]Date: ${dateFormat.format(date)}\n" +
                    "[C]Time: ${timeFormat.format(date)}\n" +
                    "[C]Paper Width: ${paperSize} mm\n" +
                    "[C]" + "-".repeat(charsPerLine) + "\n\n\n\n"
                )
                escPos.disconnectPrinter()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun openDrawer(printer: SavedPrinter): Boolean = withContext(Dispatchers.IO) {
        printMutex.withLock {
            try {
                val connection = getConnection(printer)
                connection.connect()
                connection.write(byteArrayOf(0x1B, 0x70, 0x00, 0x19, 0xFA.toByte()))
                connection.send()
                connection.disconnect()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun printReceipt(
        context: Context,
        printer: SavedPrinter,
        bill: Bill,
        items: List<BillItem>,
        resName: String,
        resAddress: String,
        resPhone: String,
        resGst: String,
        invoiceFooter: String,
        thankYouMsg: String,
        printDate: Boolean,
        printTime: Boolean,
        printCashier: Boolean,
        printPaymentMethod: Boolean,
        printQr: Boolean,
        printOrderType: Boolean,
        printItemNotes: Boolean,
        printGstBreakdown: Boolean,
        printDiscount: Boolean,
        printCustomerName: Boolean,
        printCustomerPhone: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        printMutex.withLock {
            try {
                val connection = getConnection(printer)
                connection.connect()
                val prefRepo = com.example.data.PrinterPreferencesRepository(context)
                val paperSize = prefRepo.paperSizeFlow.first()
                val charsPerLine = if (paperSize == 80) 48 else 32
                val escPos = EscPosPrinter(connection, 203, paperSize.toFloat(), charsPerLine)
                
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = Date(bill.timestamp)
                
                var itemsText = ""
                items.forEach { item ->
                    val total = CurrencyFormatter.format(item.price * item.quantity)
                    itemsText += "[L]${item.menuItemName}\n"
                    if (printItemNotes && false) {
                        itemsText += "[L]  <font size='small'>* ${""}</font>\n"
                    }
                    itemsText += "[L]${item.quantity}x[C]${CurrencyFormatter.format(item.price)}[R]$total\n"
                }
                
                var headerText = ""
                if (resName.isNotBlank()) headerText += "[C]<u><font size='big'>${resName.uppercase()}</font></u>\n"
                if (resAddress.isNotBlank()) headerText += "[C]${resAddress}\n"
                if (resPhone.isNotBlank()) headerText += "[C]Phone: ${resPhone}\n"
                if (resGst.isNotBlank()) headerText += "[C]GST: ${resGst}\n"
                
                var metaText = ""
                metaText += "[L]Invoice: #${bill.id}\n"
                if (printOrderType) metaText += "[L]Type: ${bill.orderMode}\n"
                if (printCashier) metaText += "[L]Cashier: Admin\n"
                if (printDate && printTime) {
                    metaText += "[L]Date: ${dateFormat.format(date)}[R]Time: ${timeFormat.format(date)}\n"
                } else if (printDate) {
                    metaText += "[L]Date: ${dateFormat.format(date)}\n"
                } else if (printTime) {
                    metaText += "[L]Time: ${timeFormat.format(date)}\n"
                }
                
                var footerText = ""
                if (printGstBreakdown && 0.0 > 0) {
                    footerText += "[L]GST Amount[R]${CurrencyFormatter.format(0.0)}\n"
                }
                if (printDiscount && 0.0 > 0) {
                    footerText += "[L]Discount[R]-${CurrencyFormatter.format(0.0)}\n"
                }
                if (printPaymentMethod) {
                    footerText += "[L]Payment Method[R]${"Cash"}\n"
                }
                
                var finalMsg = ""
                if (invoiceFooter.isNotBlank()) finalMsg += "[C]${invoiceFooter}\n"
                if (thankYouMsg.isNotBlank()) finalMsg += "[C]${thankYouMsg}\n"
                if (printQr) {
                    finalMsg += "[C]\n[C]<qrcode size='20'>https://example.com/invoice/${bill.id}</qrcode>\n"
                }
                
                escPos.printFormattedText(
                    headerText +
                    "[L]\n" +
                    metaText +
                    "[C]" + "-".repeat(charsPerLine) + "\n" +
                    "[L]Items[C]Price[R]Amount\n" +
                    "[C]" + "-".repeat(charsPerLine) + "\n" +
                    itemsText +
                    "[C]" + "-".repeat(charsPerLine) + "\n" +
                    footerText +
                    "[L]<font size='tall'>Grand Total</font>[R]<font size='tall'>${CurrencyFormatter.format(bill.totalAmount)}</font>\n" +
                    "[C]" + "-".repeat(charsPerLine) + "\n" +
                    "[C]\n" +
                    finalMsg +
                    "[L]\n[L]\n[L]\n"
                )
                escPos.disconnectPrinter()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun printKitchen(
        context: Context,
        printer: SavedPrinter,
        bill: Bill,
        items: List<BillItem>
    ): Boolean = withContext(Dispatchers.IO) {
        printMutex.withLock {
            try {
                val connection = getConnection(printer)
                connection.connect()
                val prefRepo = com.example.data.PrinterPreferencesRepository(context)
                val paperSize = prefRepo.paperSizeFlow.first()
                val charsPerLine = if (paperSize == 80) 48 else 32
                val escPos = EscPosPrinter(connection, 203, paperSize.toFloat(), charsPerLine)
                
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = Date(bill.timestamp)
                
                var itemsText = ""
                items.forEach { item ->
                    itemsText += "[L]${item.quantity}x <font size='tall'>${item.menuItemName}</font>\n"
                    itemsText += "[L]\n"
                }
                
                escPos.printFormattedText(
                    "[C]<u><font size='big'>KITCHEN TICKET</font></u>\n" +
                    "[L]\n" +
                    "[L]Order: #${bill.id}[R]Time: ${timeFormat.format(date)}\n" +
                    "[L]Type: ${bill.orderMode}\n" +
                    "[C]" + "-".repeat(charsPerLine) + "\n" +
                    itemsText +
                    "[C]" + "-".repeat(charsPerLine) + "\n" +
                    "[L]\n[L]\n[L]\n"
                )
                escPos.disconnectPrinter()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
