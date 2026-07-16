import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

# Replace test print bottom sheet
testprint_replacement = """
        if (showTestPrintSheet) {
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
        }
"""
content = re.sub(
    r"        if \(showTestPrintSheet\) \{[\s\S]*?\}             \}\n        \}",
    testprint_replacement.strip(),
    content
)

# Replace paper size bottom sheet
papersize_replacement = """
        if (showPaperSizeSheet) {
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
        }
"""
content = re.sub(
    r"        if \(showPaperSizeSheet\) \{[\s\S]*?\}             \}\n        \}",
    papersize_replacement.strip(),
    content
)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "r") as f:
    ps_content = f.read()

# Replace add printer dialog
addprinter_replacement = """
    if (step == 1) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = onDismiss
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md)) {
                Text("Printer Type", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
                Text("Select connection method", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                listOf("BLUETOOTH" to "Bluetooth", "USB" to "USB OTG", "WIFI" to "Network / Wi-Fi").forEach { (typeKey, typeLabel) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().liftClickable {
                            type = typeKey
                            step = 2
                        }.padding(vertical = AppTheme.spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when(typeKey) {
                                "BLUETOOTH" -> Icons.Rounded.Bluetooth
                                "USB" -> Icons.Rounded.Usb
                                else -> Icons.Rounded.Wifi
                            },
                            contentDescription = null,
                            tint = AppTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(typeLabel, style = AppTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.ChevronRight, null, tint = AppTheme.colors.textSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    } else {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = onDismiss
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md)) {
                Text("Add Printer", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
                Text("Configure details", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                Spacer(modifier = Modifier.height(16.dp))
"""
ps_content = re.sub(
    r"    com\.example\.ui\.components\.PremiumBottomSheet\([\s\S]*?        Column\(modifier = Modifier\.fillMaxWidth\(\)\.padding\(horizontal = AppTheme\.spacing\.lg, vertical = AppTheme\.spacing\.md\)\) \{\n            if \(step == 1\) \{[\s\S]*?            \} else \{",
    addprinter_replacement.strip(),
    ps_content
)

ps_content = ps_content.replace(
    r"""                com.example.ui.components.PrimaryButton(
                    text = "Save Printer",
                    onClick = {
                        if (name.isNotBlank() && address.isNotBlank()) {
                            onAdd(name, type, address, paperWidth)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}""",
    r"""                com.example.ui.components.PrimaryButton(
                    text = "Save Printer",
                    onClick = {
                        if (name.isNotBlank() && address.isNotBlank()) {
                            onAdd(name, type, address, paperWidth)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}"""
)

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "w") as f:
    f.write(ps_content)
