import re

with open("app/src/main/java/com/example/ui/screens/BillingViewModel.kt", "r") as f:
    content = f.read()

pdf_logic = """
    val isImportingPdf = MutableStateFlow(false)
    val pdfParsedItems = MutableStateFlow<List<MenuItem>>(emptyList())

    fun importFromPdf(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            isImportingPdf.value = true
            val items = com.example.util.PdfParser.parseMenuPdf(context, uri)
            pdfParsedItems.value = items
            isImportingPdf.value = false
        }
    }
    
    fun clearPdfParsedItems() {
        pdfParsedItems.value = emptyList()
    }
    
    fun updateParsedItem(index: Int, newItem: MenuItem) {
        val current = pdfParsedItems.value.toMutableList()
        if (index in current.indices) {
            current[index] = newItem
            pdfParsedItems.value = current
        }
    }
"""

content = content.replace("fun updateMenuAvailability(id: Int, isActive: Boolean) {", pdf_logic + "\n    fun updateMenuAvailability(id: Int, isActive: Boolean) {")

with open("app/src/main/java/com/example/ui/screens/BillingViewModel.kt", "w") as f:
    f.write(content)

