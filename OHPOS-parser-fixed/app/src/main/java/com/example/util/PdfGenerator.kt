package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.data.BillWithItems
import com.example.ui.screens.DayStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    suspend fun generateReportToStream(
        context: Context,
        outStream: OutputStream,
        dayStats: DayStats,
        bills: List<BillWithItems>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            var paint = Paint()
            var yPos = 50f
            
            paint.textSize = 24f
            paint.textAlign = Paint.Align.CENTER
            paint.isFakeBoldText = true
            canvas.drawText(dayStats.restaurantName, 297f, yPos, paint)
            yPos += 40f
            
            paint.textSize = 14f
            paint.isFakeBoldText = false
            canvas.drawText("Sales Report", 297f, yPos, paint)
            yPos += 25f
            
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            canvas.drawText("Period: ${dayStats.businessDate}", 297f, yPos, paint)
            yPos += 20f
            canvas.drawText("Generated: ${dateFormat.format(java.util.Date())}", 297f, yPos, paint)
            yPos += 30f
            
            paint.textAlign = Paint.Align.LEFT
            paint.isFakeBoldText = true
            canvas.drawText("Business Summary", 50f, yPos, paint)
            yPos += 20f
            canvas.drawLine(50f, yPos, 545f, yPos, paint)
            yPos += 30f
            
            paint.isFakeBoldText = false
            val summaryItems = listOf(
                "Orders:" to dayStats.orders.toString(),
                "Gross Sales:" to com.example.util.CurrencyFormatter.format(dayStats.grossSales),
                "GST Collected:" to com.example.util.CurrencyFormatter.format(dayStats.gstCollected),
                "Discounts:" to com.example.util.CurrencyFormatter.format(dayStats.discounts),
                "Cash:" to com.example.util.CurrencyFormatter.format(dayStats.cash),
                "UPI:" to com.example.util.CurrencyFormatter.format(dayStats.upi),
                "Card:" to com.example.util.CurrencyFormatter.format(dayStats.card),
                "Average Bill:" to com.example.util.CurrencyFormatter.format(dayStats.averageBill),
                "Highest Bill:" to com.example.util.CurrencyFormatter.format(dayStats.highestBill),
                "Lowest Bill:" to com.example.util.CurrencyFormatter.format(dayStats.lowestBill),
                "Top Selling Item:" to dayStats.topSellingItem,
                "Top Selling Category:" to dayStats.topSellingCategory,
                "Items Sold:" to dayStats.itemsSold.toString()
            )
            
            for ((label, value) in summaryItems) {
                canvas.drawText(label, 50f, yPos, paint)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(value, 545f, yPos, paint)
                paint.textAlign = Paint.Align.LEFT
                yPos += 20f
            }
            
            yPos += 40f
            paint.isFakeBoldText = true
            canvas.drawText("Order List", 50f, yPos, paint)
            yPos += 20f
            canvas.drawLine(50f, yPos, 545f, yPos, paint)
            yPos += 20f
            
            paint.isFakeBoldText = true
            canvas.drawText("ID", 50f, yPos, paint)
            canvas.drawText("Time", 120f, yPos, paint)
            canvas.drawText("Type", 250f, yPos, paint)
            canvas.drawText("Payment", 350f, yPos, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Amount", 545f, yPos, paint)
            paint.textAlign = Paint.Align.LEFT
            
            yPos += 20f
            paint.isFakeBoldText = false
            
            val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            
            for (billWithItems in bills) {
                if (yPos > 780f) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPos = 50f
                }
                
                val bill = billWithItems.bill
                val dateStr = java.text.SimpleDateFormat("MMM dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(bill.timestamp))
                canvas.drawText(bill.id.toString(), 50f, yPos, paint)
                canvas.drawText(dateStr, 100f, yPos, paint)
                canvas.drawText(bill.orderMode, 250f, yPos, paint)
                canvas.drawText(bill.paymentMethod, 350f, yPos, paint)
                
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(com.example.util.CurrencyFormatter.format(bill.totalAmount), 545f, yPos, paint)
                paint.textAlign = Paint.Align.LEFT
                
                yPos += 20f
            }
            
            document.finishPage(page)
            document.writeTo(outStream)
            outStream.close()
            document.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun generateDailyReport(
        context: Context,
        folderUri: Uri,
        dayStats: DayStats,
        bills: List<BillWithItems>
    ): String? = withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            var paint = Paint()
            var yPos = 50f
            
            paint.textSize = 24f
            paint.textAlign = Paint.Align.CENTER
            paint.isFakeBoldText = true
            canvas.drawText(dayStats.restaurantName, 297f, yPos, paint)
            yPos += 40f
            
            paint.textSize = 14f
            paint.isFakeBoldText = false
            canvas.drawText("Daily Closing Report", 297f, yPos, paint)
            yPos += 25f
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            canvas.drawText("Business Date: ${dayStats.businessDate}", 297f, yPos, paint)
            yPos += 20f
            canvas.drawText("Generated: ${dateFormat.format(Date())}", 297f, yPos, paint)
            yPos += 30f
            
            paint.textAlign = Paint.Align.LEFT
            paint.isFakeBoldText = true
            canvas.drawText("Business Summary", 50f, yPos, paint)
            yPos += 20f
            canvas.drawLine(50f, yPos, 545f, yPos, paint)
            yPos += 30f
            
            paint.isFakeBoldText = false
            val summaryItems = listOf(
                "Orders:" to dayStats.orders.toString(),
                "Gross Sales:" to CurrencyFormatter.format(dayStats.grossSales),
                "GST Collected:" to CurrencyFormatter.format(dayStats.gstCollected),
                "Discounts:" to CurrencyFormatter.format(dayStats.discounts),
                "Cash:" to CurrencyFormatter.format(dayStats.cash),
                "UPI:" to CurrencyFormatter.format(dayStats.upi),
                "Card:" to CurrencyFormatter.format(dayStats.card),
                "Average Bill:" to CurrencyFormatter.format(dayStats.averageBill),
                "Highest Bill:" to CurrencyFormatter.format(dayStats.highestBill),
                "Lowest Bill:" to CurrencyFormatter.format(dayStats.lowestBill),
                "Top Selling Item:" to dayStats.topSellingItem,
                "Top Selling Category:" to dayStats.topSellingCategory,
                "Items Sold:" to dayStats.itemsSold.toString()
            )
            
            for ((label, value) in summaryItems) {
                canvas.drawText(label, 50f, yPos, paint)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(value, 545f, yPos, paint)
                paint.textAlign = Paint.Align.LEFT
                yPos += 20f
            }
            
            yPos += 20f
            paint.isFakeBoldText = true
            canvas.drawText("Order History", 50f, yPos, paint)
            yPos += 20f
            canvas.drawLine(50f, yPos, 545f, yPos, paint)
            yPos += 30f
            
            paint.isFakeBoldText = false
            for (billWithItems in bills.sortedBy { it.bill.timestamp }) {
                val b = billWithItems.bill
                if (yPos > 780f) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPos = 50f
                }
                
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(b.timestamp))
                val lineStr = "$timeStr | Inv: ${b.id} | ${b.paymentMethod} | Items: ${b.totalItems} | GST: ${CurrencyFormatter.formatNoDecimals(b.gstAmount)} | Total: ${CurrencyFormatter.formatNoDecimals(b.totalAmount)}"
                canvas.drawText(lineStr, 50f, yPos, paint)
                yPos += 20f
            }
            
            yPos += 20f
            if (yPos > 750f) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPos = 50f
            }
            
            paint.isFakeBoldText = true
            canvas.drawLine(50f, yPos, 545f, yPos, paint)
            yPos += 30f
            
            val finalItems = listOf(
                "Total Orders:" to dayStats.orders.toString(),
                "Total GST:" to CurrencyFormatter.format(dayStats.gstCollected),
                "Total Discounts:" to CurrencyFormatter.format(dayStats.discounts),
                "Net Sales:" to CurrencyFormatter.format(dayStats.grossSales - dayStats.gstCollected),
                "Grand Total:" to CurrencyFormatter.format(dayStats.grossSales)
            )
            
            for ((label, value) in finalItems) {
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText(label, 50f, yPos, paint)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(value, 545f, yPos, paint)
                yPos += 20f
            }
            
            document.finishPage(page)
            
            val df = DocumentFile.fromTreeUri(context, folderUri)
            val fileName = "OHPOS_Daily_Report_${dayStats.businessDate}.pdf"
            var newFile = df?.findFile(fileName)
            if (newFile == null) {
                newFile = df?.createFile("application/pdf", fileName)
            }
            
            if (newFile != null) {
                val outStream: OutputStream? = context.contentResolver.openOutputStream(newFile.uri)
                if (outStream != null) {
                    document.writeTo(outStream)
                    outStream.close()
                    document.close()
                    return@withContext newFile.uri.toString()
                }
            }
            
            document.close()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun generateInvoicePdf(context: Context, billWithItems: BillWithItems): java.io.File = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        var yPos = 50f
        
        paint.textSize = 24f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        canvas.drawText("INVOICE", 297f, yPos, paint)
        yPos += 40f
        
        paint.textSize = 14f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        canvas.drawText("Invoice #${billWithItems.bill.id}", 297f, yPos, paint)
        yPos += 20f
        canvas.drawText("Date: ${dateFormat.format(Date(billWithItems.bill.timestamp))}", 297f, yPos, paint)
        yPos += 40f
        
        paint.textAlign = Paint.Align.LEFT
        paint.isFakeBoldText = true
        canvas.drawText("Item", 50f, yPos, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Qty", 350f, yPos, paint)
        canvas.drawText("Price", 450f, yPos, paint)
        canvas.drawText("Total", 545f, yPos, paint)
        yPos += 20f
        canvas.drawLine(50f, yPos, 545f, yPos, paint)
        yPos += 20f
        
        paint.isFakeBoldText = false
        for (item in billWithItems.items) {
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(item.menuItemName, 50f, yPos, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(item.quantity.toString(), 350f, yPos, paint)
            canvas.drawText(CurrencyFormatter.formatNoDecimals(item.price), 450f, yPos, paint)
            canvas.drawText(CurrencyFormatter.formatNoDecimals(item.price * item.quantity), 545f, yPos, paint)
            yPos += 20f
        }
        
        yPos += 20f
        canvas.drawLine(50f, yPos, 545f, yPos, paint)
        yPos += 30f
        
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Total", 50f, yPos, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(CurrencyFormatter.formatNoDecimals(billWithItems.bill.totalAmount), 545f, yPos, paint)
        
        document.finishPage(page)
        
        val file = java.io.File(context.cacheDir, "Invoice_${billWithItems.bill.id}.pdf")
        val outStream = java.io.FileOutputStream(file)
        document.writeTo(outStream)
        outStream.close()
        document.close()
        
        file
    }
}
