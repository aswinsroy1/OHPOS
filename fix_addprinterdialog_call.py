import re

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "r") as f:
    content = f.read()

content = content.replace("AddPrinterDialog(\n                onDismissRequest = { showAddDialog = false },", "AddPrinterDialog(\n                onDismiss = { showAddDialog = false },")

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "w") as f:
    f.write(content)
