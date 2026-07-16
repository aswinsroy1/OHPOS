import re
with open("app/src/main/java/com/example/util/PrinterManager.kt", "r") as f:
    content = f.read()

# Replace paperWidth usage with one from prefRepo.
content = content.replace("val charsPerLine = if (printer.paperWidth == 80) 48 else 32", 
"""val prefRepo = com.example.data.PrinterPreferencesRepository(context)
                val paperSize = kotlinx.coroutines.flow.first(prefRepo.paperSizeFlow)
                val charsPerLine = if (paperSize == 80) 48 else 32""")
content = content.replace("printer.paperWidth.toFloat()", "paperSize.toFloat()")
content = content.replace("printer.paperWidth} mm", "${paperSize} mm")

with open("app/src/main/java/com/example/util/PrinterManager.kt", "w") as f:
    f.write(content)
