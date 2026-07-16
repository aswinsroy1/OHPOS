with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

rb_58 = """androidx.compose.material3.RadioButton(
                            selected = currentPaperSize == 58, 
                            onClick = { scope.launch { prefRepo.setPaperSize(58); showPaperSizeSheet = false } },
                            colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                selectedColor = AppTheme.colors.accent,
                                unselectedColor = AppTheme.colors.textSecondary
                            )
                        )"""
content = content.replace("androidx.compose.material3.RadioButton(selected = currentPaperSize == 58, onClick = { scope.launch { prefRepo.setPaperSize(58); showPaperSizeSheet = false } })", rb_58)

rb_80 = """androidx.compose.material3.RadioButton(
                            selected = currentPaperSize == 80, 
                            onClick = { scope.launch { prefRepo.setPaperSize(80); showPaperSizeSheet = false } },
                            colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                selectedColor = AppTheme.colors.accent,
                                unselectedColor = AppTheme.colors.textSecondary
                            )
                        )"""
content = content.replace("androidx.compose.material3.RadioButton(selected = currentPaperSize == 80, onClick = { scope.launch { prefRepo.setPaperSize(80); showPaperSizeSheet = false } })", rb_80)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
