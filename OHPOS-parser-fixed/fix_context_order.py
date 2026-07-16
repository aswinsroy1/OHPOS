with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "r") as f:
    content = f.read()

replacement = """
    val context = LocalContext.current
    val prefRepo = remember { PrinterPreferencesRepository(context) }
    val printCustomerCopy by prefRepo.printCustomerCopyFlow.collectAsState(initial = false)
    val printKitchenCopy by prefRepo.printKitchenCopyFlow.collectAsState(initial = false)
    val openDrawer by prefRepo.openDrawerFlow.collectAsState(initial = false)
    val scope = rememberCoroutineScope()
"""

content = content.replace("""    val prefRepo = remember { PrinterPreferencesRepository(context) }
    val printCustomerCopy by prefRepo.printCustomerCopyFlow.collectAsState(initial = false)
    val printKitchenCopy by prefRepo.printKitchenCopyFlow.collectAsState(initial = false)
    val openDrawer by prefRepo.openDrawerFlow.collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current""", replacement)

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "w") as f:
    f.write(content)
