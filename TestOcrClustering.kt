import kotlin.math.abs

data class Rect(val left: Int, val top: Int, val right: Int, val bottom: Int)
data class Line(val text: String, val boundingBox: Rect)

fun main() {
    val pageWidth = 1000
    val pageWidthTolerance = pageWidth * 0.15f
    
    // Simulate the exact failure pattern:
    // "THE COLD MENU" centered at top.
    // Column 1 (Left): left edge ~100.
    // Column 2 (Right): left edge ~600.
    // Interleaving causes "F" (from 'F' in something) and "THE COLD MENU" to break.
    
    val lines = listOf(
        Line("THE COLD MENU", Rect(400, 50, 600, 100)), // Centered header
        Line("F", Rect(100, 120, 120, 150)),            // Very short item in col 1
        Line("Fresh Salad", Rect(600, 125, 750, 150)),  // Col 2 item
        Line("12.00", Rect(800, 125, 850, 150)),        // Col 2 price
        Line("ish Tacos", Rect(130, 120, 250, 150)),    // Wait, the "F" issue might have been "Fish Tacos" split into "F" and "ish Tacos"?
        // Actually, if "F" was a single letter, it's just "F". Let's put some data
        Line("ish Tacos 10.00", Rect(130, 120, 250, 150)), // Let's just say "F" was a standalone item
        Line("F 10.00", Rect(100, 120, 150, 150)),
        Line("Ceviche 15.00", Rect(100, 180, 250, 210)),
        Line("Oysters 20.00", Rect(600, 180, 750, 210))
    )
    
    println("--- START TRACE (Multi-Column Cold Menu) ---")
    val clusters = mutableListOf<MutableList<Line>>()
    
    for (line in lines) {
        val left = line.boundingBox.left.toFloat()
        var placed = false
        for (cluster in clusters) {
            val clusterLeft = cluster.map { it.boundingBox.left }.average().toFloat()
            if (abs(left - clusterLeft) <= pageWidthTolerance) {
                cluster.add(line)
                placed = true
                break
            }
        }
        if (!placed) {
            clusters.add(mutableListOf(line))
        }
    }
    
    println("Total Clusters Found: ${clusters.size}")
    clusters.forEachIndexed { i, c ->
        println("Cluster $i (avg left: ${c.map{it.boundingBox.left}.average()}):")
        c.forEach { println("  - '${it.text}' (Left: ${it.boundingBox.left}, Top: ${it.boundingBox.top})") }
    }
    
    val actualColumns = clusters.filter { it.size > 2 }.sortedBy { c -> c.map { it.boundingBox.left }.average() }
    println("\nActual Columns Identified (size > 2): ${actualColumns.size}")
    
    val headerLines = clusters.filter { it.size <= 2 }.flatten().sortedBy { it.boundingBox.top }
    var globalCategory = "Uncategorized"
    
    println("\nHeaders Identified:")
    for (h in headerLines) {
        println("  - '${h.text}'")
        val text = h.text.trim()
        if (text.length in 3..30 && !text.contains(Regex("""\d"""))) {
            globalCategory = text.split(" ").joinToString(" ") { 
                it.replaceFirstChar { char -> if (char.isLowerCase()) char.uppercase() else char.toString() } 
            }
        }
    }
    println("Extracted Global Category: '$globalCategory'")
    
    // Single column trace
    println("\n--- START TRACE (Single Column Fallback) ---")
    val singleLines = listOf(
        Line("THE HOT MENU", Rect(100, 50, 400, 100)),
        Line("Burger 15.00", Rect(100, 120, 300, 150)),
        Line("Fries 5.00", Rect(100, 180, 250, 210))
    )
    val singleClusters = mutableListOf<MutableList<Line>>()
    for (line in singleLines) {
        val left = line.boundingBox.left.toFloat()
        var placed = false
        for (cluster in singleClusters) {
            val clusterLeft = cluster.map { it.boundingBox.left }.average().toFloat()
            if (abs(left - clusterLeft) <= pageWidthTolerance) {
                cluster.add(line)
                placed = true
                break
            }
        }
        if (!placed) {
            singleClusters.add(mutableListOf(line))
        }
    }
    println("Single Menu Clusters Found: ${singleClusters.size}")
    if (singleClusters.size <= 1) {
        println("Result: Successfully fell back to flat parser.")
    }
}
