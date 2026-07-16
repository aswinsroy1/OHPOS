package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import com.example.BuildConfig
import com.example.data.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class NoTextFoundException(message: String) : Exception(message)

object PdfParser {

    suspend fun parseMenuPdf(context: Context, uri: Uri): List<MenuItem> = withContext(Dispatchers.IO) {
        val bitmaps = mutableListOf<Bitmap>()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        try {
            val tempFile = File(context.cacheDir, "temp_pdf_${System.currentTimeMillis()}.pdf")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)

            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(
                    page.width * 2,
                    page.height * 2,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)
                page.close()
            }
            tempFile.delete()
        } finally {
            renderer?.close()
            pfd?.close()
        }

        processBitmapsThroughOCR(bitmaps)
    }

    suspend fun parseMenuImages(context: Context, uris: List<Uri>): List<MenuItem> = withContext(Dispatchers.IO) {
        val bitmaps = mutableListOf<Bitmap>()
        for (uri in uris) {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            bitmaps.add(bitmap)
        }
        processBitmapsThroughOCR(bitmaps)
    }

    private suspend fun processBitmapsThroughOCR(bitmaps: List<Bitmap>): List<MenuItem> = withContext(Dispatchers.IO) {
        if (bitmaps.isEmpty()) throw Exception("No images could be processed.")

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val allItems = mutableListOf<MenuItem>()

        for (bitmap in bitmaps) {
            val image = InputImage.fromBitmap(bitmap, 0)
            try {
                val result = recognizer.process(image).await()
                val items = parsePageResult(result, bitmap.width)
                allItems.addAll(items)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (allItems.isEmpty()) {
            throw NoTextFoundException("No menu items with valid prices were detected in the selected PDF, image or camera scan.\n\nPlease make sure the menu is clearly visible and that prices are included.")
        }

        return@withContext allItems
    }

    private fun parsePageResult(result: com.google.mlkit.vision.text.Text, pageWidth: Int): List<MenuItem> {
        val parsedItems = RowSegmentMenuParser.parse(result, pageWidth)
        if (parsedItems.isNotEmpty()) {
            return parsedItems.map { parsed ->
                MenuItem(
                    id = 0,
                    name = parsed.name,
                    price = parsed.price,
                    category = parsed.category,
                    description = "",
                    imageUrl = "",
                    isActive = true
                )
            }
        }
        // Last-resort fallback only if the row/segment parser found nothing at all
        // (e.g. a single stray line with no rows detected) - flat parse as before.
        val textBlocksStr = result.textBlocks.joinToString("\n") { it.text }
        return parseTextLocallyFlat(textBlocksStr)
    }

    private fun parseTextLocallyFlat(text: String, initialCategory: String = "Uncategorized"): List<MenuItem> {
        val items = mutableListOf<MenuItem>()
        var currentCategory = initialCategory

        val lines = text.split('\n').map { it.trim() }.filter { it.isNotEmpty() }

        // Regex to match lines ending with a price.
        val pricePattern = Regex("""^(.*?)(?:[\s\.\-,]*(?:Rs\.?|₹|INR|\$)?\s*[\.\-,]*\s*)(\d+(?:\.\d{1,2})?)\s*$""", RegexOption.IGNORE_CASE)

        for (line in lines) {
            val matchResult = pricePattern.find(line)
            if (matchResult != null) {
                var itemName = matchResult.groupValues[1].trim()
                val priceStr = matchResult.groupValues[2]

                itemName = itemName.replace(Regex("""[\.\-\s,:;]+$"""), "").trim()

                val price = priceStr.toDoubleOrNull()

                if (itemName.isNotBlank() && price != null && price > 0) {
                    items.add(MenuItem(
                        id = 0,
                        name = itemName,
                        price = price,
                        category = currentCategory,
                        description = "",
                        imageUrl = "",
                        isActive = true
                    ))
                }
            } else {
                if (line.length in 3..30 && !line.contains(Regex("""\d"""))) {
                    currentCategory = line.split(" ").joinToString(" ") { 
                        it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(java.util.Locale.getDefault()) else char.toString() } 
                    }
                }
            }
        }

        return items
    }
}
