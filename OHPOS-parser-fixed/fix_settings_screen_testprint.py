import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

# Need to inject logic for Test Print
import_stmt = "import com.example.ui.screens.PrinterViewModel\nimport com.example.data.SavedPrinter\n"
content = content.replace("package com.example.ui.screens", "package com.example.ui.screens\n" + import_stmt)

# Add showTestPrintSheet state
vars_stmt = """
    var showTestPrintSheet by remember { mutableStateOf(false) }
    val printerViewModel: PrinterViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val savedPrinters by printerViewModel.savedPrinters.collectAsState()
    val isPrinting by printerViewModel.isPrinting.collectAsState()
    val printError by printerViewModel.printError.collectAsState()
    
    LaunchedEffect(printError) {
        if (printError != null) {
            Toast.makeText(context, printError, Toast.LENGTH_LONG).show()
            printerViewModel.clearError()
        }
    }
    
    val allSections = listOf(
"""

content = content.replace("    val allSections = listOf(", vars_stmt)

# Update onClick
content = content.replace(
    'SettingItemData("Test Print", Icons.Rounded.DoneAll)',
    'SettingItemData("Test Print", Icons.Rounded.DoneAll, onClick = { if (savedPrinters.isEmpty()) Toast.makeText(context, "No printer configured.", Toast.LENGTH_LONG).show() else if (savedPrinters.size == 1) printerViewModel.testPrint(context, savedPrinters.first()) else showTestPrintSheet = true })'
)

# Add Bottom Sheet
sheet_stmt = """
        if (showTestPrintSheet) {
            com.example.ui.components.PremiumBottomSheet(
                onDismiss = { showTestPrintSheet = false },
                title = "Select Printer",
                subtitle = "Choose a printer for the test print"
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md)) {
                    savedPrinters.forEach { printer ->
                        Row(
                            modifier = Modifier.fillMaxWidth().liftClickable { 
                                printerViewModel.testPrint(context, printer)
                                showTestPrintSheet = false 
                            }.padding(vertical = AppTheme.spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Print, contentDescription = null, tint = AppTheme.colors.textSecondary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(printer.name, style = AppTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
                        }
                    }
                }
            }
        }
    }
}
"""

content = content.replace("    }\n}\n\n@Composable\nfun SettingsRow(", sheet_stmt + "\n@Composable\nfun SettingsRow(")

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
