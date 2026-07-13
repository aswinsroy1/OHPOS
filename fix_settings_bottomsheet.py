import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

# Replace showPaperSizeSheet block
replacement_paper = """        if (showPaperSizeSheet) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showPaperSizeSheet = false }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md)) {
                    Text("Select Paper Size", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
                    Text("Changing paper size adjusts receipt formatting automatically.", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().liftClickable { scope.launch { prefRepo.setPaperSize(58); showPaperSizeSheet = false } }.padding(vertical = AppTheme.spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(selected = currentPaperSize == 58, onClick = { scope.launch { prefRepo.setPaperSize(58); showPaperSizeSheet = false } })
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("58 mm", style = AppTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AppTheme.colors.divider))
                    Row(
                        modifier = Modifier.fillMaxWidth().liftClickable { scope.launch { prefRepo.setPaperSize(80); showPaperSizeSheet = false } }.padding(vertical = AppTheme.spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(selected = currentPaperSize == 80, onClick = { scope.launch { prefRepo.setPaperSize(80); showPaperSizeSheet = false } })
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("80 mm", style = AppTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }"""
content = re.sub(
    r"        if \(showPaperSizeSheet\) \{[\s\S]*?\}             \}\n        \}",
    replacement_paper,
    content
)

# Replace showTestPrintSheet block
replacement_test = """        if (showTestPrintSheet) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showTestPrintSheet = false }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md)) {
                    Text("Select Printer", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
                    Text("Choose a printer for the test print", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
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
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }"""
content = re.sub(
    r"        if \(showTestPrintSheet\) \{[\s\S]*?\}             \}\n        \}",
    replacement_test,
    content
)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
