import re

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "r") as f:
    content = f.read()

content = content.replace("fun AddPrinterDialog(\n    onDismiss:", "fun AddPrinterDialog(\n    onDismissRequest:")

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "w") as f:
    f.write(content)
