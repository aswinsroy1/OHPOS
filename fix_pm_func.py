import re

with open("app/src/main/java/com/example/util/PrinterManager.kt", "r") as f:
    content = f.read()

func_pattern = re.compile(r"suspend fun printTest\([\s\S]*?\} catch \(e: Exception\) \{", re.MULTILINE)

replacement = """suspend fun printTest(context: android.content.Context, printer: SavedPrinter): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val connection = connect(context, printer) ?: return@withContext false
            val charsPerLine = if (printer.paperWidth == 80) 48 else 32
            val escPos = com.dantsu.escposprinter.EscPosPrinter(connection, 203, printer.paperWidth.toFloat(), charsPerLine)
            
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            val date = java.util.Date(System.currentTimeMillis())
            escPos.printFormattedText(
                "[C]--------------------------------\\n" +
                "[C]<u><font size='big'>OH POS</font></u>\\n" +
                "[C]Printer Test\\n" +
                "[C]\\n" +
                "[C]Printer Connected Successfully\\n" +
                "[C]\\n" +
                "[C]Date: ${dateFormat.format(date)}\\n" +
                "[C]Time: ${timeFormat.format(date)}\\n" +
                "[C]Paper Width: ${printer.paperWidth} mm\\n" +
                "[C]--------------------------------\\n\\n\\n\\n"
            )
            escPos.disconnectPrinter()
            true
        } catch (e: Exception) {"""

content = func_pattern.sub(replacement, content)

with open("app/src/main/java/com/example/util/PrinterManager.kt", "w") as f:
    f.write(content)
