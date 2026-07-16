import re
with open("app/src/main/java/com/example/util/PrinterManager.kt", "r") as f:
    content = f.read()

content = content.replace('"[C]--------------------------------\\n\\n\\n\\n"', '"[C]" + "-".repeat(charsPerLine) + "\\n\\n\\n\\n"')

with open("app/src/main/java/com/example/util/PrinterManager.kt", "w") as f:
    f.write(content)
