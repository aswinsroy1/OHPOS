import re

with open('app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt', 'r') as f:
    content = f.read()

target = """    LaunchedEffect(printError) {
        if (printError != null) {
            Toast.makeText(context, printError, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }"""

replacement = """"""

content = content.replace(target, replacement)

dialog_ui = """
    if (printError != null) {
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
    }
"""

idx = content.rfind("    Box(")
content = content[:idx] + dialog_ui + content[idx:]

with open('app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt', 'w') as f:
    f.write(content)
