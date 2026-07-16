import re

with open("app/src/main/java/com/example/util/PdfParser.kt", "r") as f:
    text = f.read()

target = """    private suspend fun processBitmapsThroughOCR(bitmaps: List<Bitmap>): List<MenuItem> = withContext(Dispatchers.IO) {
        if (bitmaps.isEmpty()) throw Exception("No images could be processed.")
        
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val stringBuilder = StringBuilder()
        
        for (bitmap in bitmaps) {
            val image = InputImage.fromBitmap(bitmap, 0)
            try {
                val result = recognizer.process(image).await()
                stringBuilder.append(result.text).append("\n")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        val extractedText = stringBuilder.toString().trim()
        if (extractedText.isBlank()) {
            throw NoTextFoundException("No menu items with valid prices were detected in the selected PDF, image or camera scan.\n\nPlease make sure the menu is clearly visible and that prices are included.")
        }
        
        parseTextLocally(extractedText)
    }
    
    private fun parseTextLocally(text: String): List<MenuItem> {"""

replacement = """    private suspend fun processBitmapsThroughOCR(bitmaps: List<Bitmap>): List<MenuItem> = withContext(Dispatchers.IO) {
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
        val lines = result.textBlocks.flatMap { it.lines }
        if (lines.isEmpty()) return emptyList()

        val pageWidthTolerance = pageWidth * 0.15f
        
        // 1. Cluster by left-edge x-position
        val clusters = mutableListOf<MutableList<com.google.mlkit.vision.text.Text.Line>>()
        
        for (line in lines) {
            val left = line.boundingBox?.left?.toFloat() ?: 0f
            var placed = false
            for (cluster in clusters) {
                val clusterLeft = cluster.mapNotNull { it.boundingBox?.left }.average().toFloat()
                if (Math.abs(left - clusterLeft) <= pageWidthTolerance) {
                    cluster.add(line)
                    placed = true
                    break
                }
            }
            if (!placed) {
                clusters.add(mutableListOf(line))
            }
        }
        
        val textBlocksStr = result.textBlocks.joinToString("\n") { it.text }

        // Fallback to flat parsing if 1 or 0 clusters detected
        if (clusters.size <= 1) {
            return parseTextLocallyFlat(textBlocksStr)
        }
        
        // 2. Identify real columns vs stray headers. Real columns typically have > 2 lines
        val actualColumns = clusters.filter { it.size > 2 }.sortedBy { cluster ->
            cluster.mapNotNull { it.boundingBox?.left }.average()
        }
        
        // Fallback if we only found 1 real column
        if (actualColumns.size <= 1) {
            return parseTextLocallyFlat(textBlocksStr)
        }
        
        // 3. Process stray headers that might act as global page categories
        // Centered headers will form their own cluster because their left edge is far from the columns
        val headerLines = clusters.filter { it.size <= 2 }.flatten().sortedBy { it.boundingBox?.top ?: 0 }
        
        var globalCategory = "Uncategorized"
        
        for (headerLine in headerLines) {
            val text = headerLine.text.trim()
            if (text.length in 3..30 && !text.contains(Regex(\"\"\"\\d\"\"\"))) {
                globalCategory = text.split(" ").joinToString(" ") { 
                    it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(java.util.Locale.getDefault()) else char.toString() } 
                }
            }
        }

        val items = mutableListOf<MenuItem>()
        
        // 4. Sort each column vertically and parse independently
        for (col in actualColumns) {
            col.sortBy { it.boundingBox?.top ?: 0 }
            val colText = col.joinToString("\n") { it.text }
            val colItems = parseTextLocallyFlat(colText, globalCategory)
            items.addAll(colItems)
        }
        
        return items
    }
    
    private fun parseTextLocallyFlat(text: String, initialCategory: String = "Uncategorized"): List<MenuItem> {"""

text = text.replace(target, replacement)
text = text.replace("var currentCategory = \"Uncategorized\"", "var currentCategory = initialCategory")

with open("app/src/main/java/com/example/util/PdfParser.kt", "w") as f:
    f.write(text)
