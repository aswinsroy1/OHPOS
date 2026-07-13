with open("app/src/main/java/com/example/ui/components/PinEntryDialog.kt", "r") as f:
    content = f.read()

content = content.replace("isSetupMode: Boolean = false,", "isSetupMode: Boolean = false,\n    externalError: Boolean = false,")

content = content.replace("var showError by remember { mutableStateOf(false) }", "var showError by remember { mutableStateOf(false) }\n    LaunchedEffect(externalError) {\n        if (externalError) {\n            errorMessage = \"Incorrect PIN\"\n            shake()\n        }\n    }")

with open("app/src/main/java/com/example/ui/components/PinEntryDialog.kt", "w") as f:
    f.write(content)
