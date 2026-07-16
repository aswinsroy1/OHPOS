import re
with open("app/src/main/java/com/example/util/PrinterManager.kt", "r") as f:
    content = f.read()

# Replace "[C]--------------------------------\n" with "[C]" + "-".repeat(charsPerLine) + "\n"
content = content.replace('"[C]--------------------------------\\n"', '"[C]" + "-".repeat(charsPerLine) + "\\n"')
content = content.replace('"[C]================================\\n"', '"[C]" + "=".repeat(charsPerLine) + "\\n"')

with open("app/src/main/java/com/example/util/PrinterManager.kt", "w") as f:
    f.write(content)
