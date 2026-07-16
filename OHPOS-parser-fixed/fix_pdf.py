import re
with open("app/src/main/java/com/example/util/PdfGenerator.kt", "r") as f:
    content = f.read()

replacement = """import kotlinx.coroutines.flow.first
import com.example.data.PrinterPreferencesRepository

object PdfGenerator {
    suspend fun generateInvoicePdf(context: Context, billWithItems: BillWithItems): File {
        val prefRepo = PrinterPreferencesRepository(context)
        val paperSize = prefRepo.paperSizeFlow.first()
        
        val widthPoints = if (paperSize == 80) 226 else 164 // 80mm or 58mm
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(widthPoints, 842, 1).create() // Fixed height for now, wait, thermal paper is continuous. But PDF needs a height. Let's calculate height based on items.
"""
# I should write a proper script to replace this.
