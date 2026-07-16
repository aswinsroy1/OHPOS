import re

with open("app/src/main/java/com/example/util/PrinterManager.kt", "r") as f:
    content = f.read()

replacement = """
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = Date(System.currentTimeMillis())
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
"""

content = re.sub(
    r"                val timeFormat = SimpleDateFormat\(\"HH:mm\", Locale\.getDefault\(\)\)\n                val date = Date\(System\.currentTimeMillis\(\)\)\n                escPos\.printFormattedText\([\s\S]*?\n                \)",
    replacement.strip("\n"),
    content
)

with open("app/src/main/java/com/example/util/PrinterManager.kt", "w") as f:
    f.write(content)
