import re

with open('app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt', 'r') as f:
    content = f.read()

# The dialog UI was injected before the last `Box(` which is in `PrinterCard`.
# Let's remove the dialog UI and put it in the right place.

dialog_ui = """    if (printError != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewModel.clearError() },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.spacing.lg)
                    .clip(AppTheme.radius.xl)
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.borderLight, AppTheme.radius.xl)
                    .padding(AppTheme.spacing.xl)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (printError!!.contains("Offline")) Icons.Rounded.PrintDisabled else Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (printError!!.contains("Offline")) "Printer Offline" else "Error",
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = printError!!.replace("Printer Offline\\n", ""),
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    com.example.ui.components.PrimaryButton(
                        text = "OK",
                        onClick = { viewModel.clearError() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }"""

# Remove it first
content = content.replace(dialog_ui, "")

# Now find where PrinterSettingsScreen ends. It's before `fun PrinterCard`
# Let's put it right before `} // End of Box` or right before `fun PrinterCard`.
# Actually `PrinterSettingsScreen` returns a `Box` as the root element.
# The Box ends right before `fun PrinterCard`.

printer_card_idx = content.find("fun PrinterCard(")
# The previous line should be a closing brace `}`.
insert_idx = content.rfind("}", 0, printer_card_idx)

content = content[:insert_idx] + dialog_ui + "\n" + content[insert_idx:]

with open('app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt', 'w') as f:
    f.write(content)

