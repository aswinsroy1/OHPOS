with open("app/src/main/java/com/example/ui/components/PinEntryDialog.kt", "r") as f:
    content = f.read()

content = content.replace("AppTheme.colors.error", "Color(0xFFE57373)")

content = content.replace("    var showError by remember { mutableStateOf(false) }\n    var showError by remember { mutableStateOf(false) }", "    var showError by remember { mutableStateOf(false) }")

with open("app/src/main/java/com/example/ui/components/PinEntryDialog.kt", "w") as f:
    f.write(content)
