with open("app/src/main/java/com/example/util/PdfGenerator.kt", "w") as f:
    f.write("""package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.BillWithItems
import com.example.data.PrinterPreferencesRepository
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {
    suspend fun generateInvoicePdf(context: Context, billWithItems: BillWithItems): File {
        val prefRepo = PrinterPreferencesRepository(context)
        val paperSize = prefRepo.paperSizeFlow.first()
        
        val is80 = paperSize == 80
        val width = if (is80) 226 else 164
        val margin = if (is80) 12f else 8f
        
        // Calculate dynamic height
        var estHeight = 250f + (billWithItems.items.size * 30f) + 150f
        
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(width, estHeight.toInt(), 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        paint.color = Color.BLACK
        val appName = context.getString(context.applicationInfo.labelRes)
        
        // Title
        paint.textSize = if (is80) 14f else 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(appName, width / 2f, 30f, paint)
        
        paint.textSize = if (is80) 12f else 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Invoice", width / 2f, 50f, paint)
        
        // Order Info
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = if (is80) 10f else 8f
        val date = Date(billWithItems.bill.timestamp)
        val formatter = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        
        var yPos = 80f
        canvas.drawText("Order #${billWithItems.bill.id}", margin, yPos, paint); yPos += 12f
        canvas.drawText("Date: ${formatter.format(date)}", margin, yPos, paint); yPos += 12f
        canvas.drawText("Type: ${billWithItems.bill.orderMode}", margin, yPos, paint); yPos += 20f
        
        // Table Header
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val qtyX = if (is80) width - margin - 80f else width - margin - 60f
        val priceX = if (is80) width - margin - 40f else width - margin - 30f
        val totalX = width - margin
        
        canvas.drawText("Item", margin, yPos, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Qty", qtyX, yPos, paint)
        canvas.drawText("Price", priceX, yPos, paint)
        canvas.drawText("Total", totalX, yPos, paint)
        paint.textAlign = Paint.Align.LEFT
        
        // Table Items
        yPos += 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for (item in billWithItems.items) {
            val name = item.menuItemName
            // Handle wrapping manually for simple PDF
            val maxNameWidth = qtyX - margin - 5f
            var currentName = name
            if (paint.measureText(currentName) > maxNameWidth) {
                while (paint.measureText(currentName + "...") > maxNameWidth && currentName.isNotEmpty()) {
                    currentName = currentName.substring(0, currentName.length - 1)
                }
                currentName += "..."
            }
            
            canvas.drawText(currentName, margin, yPos, paint)
            
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(item.quantity.toString(), qtyX, yPos, paint)
            canvas.drawText(CurrencyFormatter.formatNoDecimals(item.price), priceX, yPos, paint)
            val total = item.price * item.quantity
            canvas.drawText(CurrencyFormatter.formatNoDecimals(total), totalX, yPos, paint)
            paint.textAlign = Paint.Align.LEFT
            
            yPos += 15f
        }
        
        // Summary
        yPos += 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val subtotal = billWithItems.items.sumOf { it.price * it.quantity }
        val gst = billWithItems.bill.totalAmount - subtotal
        
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Subtotal:", priceX, yPos, paint)
        canvas.drawText(CurrencyFormatter.formatNoDecimals(subtotal), totalX, yPos, paint)
        
        yPos += 15f
        canvas.drawText("GST (5%):", priceX, yPos, paint)
        canvas.drawText(CurrencyFormatter.formatNoDecimals(gst), totalX, yPos, paint)
        
        yPos += 15f
        paint.textSize = if (is80) 12f else 10f
        canvas.drawText("Grand Total:", priceX, yPos, paint)
        canvas.drawText(CurrencyFormatter.formatNoDecimals(billWithItems.bill.totalAmount), totalX, yPos, paint)
        
        document.finishPage(page)
        
        val file = File(context.cacheDir, "invoice_${billWithItems.bill.id}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()
        
        return file
    }
}
""")
