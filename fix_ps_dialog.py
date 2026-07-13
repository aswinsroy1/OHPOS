with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "r") as f:
    content = f.read()

# Make the call match the parameter
content = content.replace("onDismiss = { showAddDialog = false }", "onDismissRequest = { showAddDialog = false }")
# Make the internals match the parameter
content = content.replace("onDismiss()", "onDismissRequest()")

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "w") as f:
    f.write(content)
