import re

with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'r') as f:
    content = f.read()

# Replace the LaunchedEffect(printEvent) and showToast block
target = """    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    LaunchedEffect(printEvent) {
        when (printEvent) {
            is PrintEvent.Success -> {
                toastMessage = "✓ Bill printed successfully"
                showToast = true
                viewModel.clearPrintEvent()
            }
            is PrintEvent.NoPrinter -> {
                toastMessage = "Bill saved. No printer connected."
                showToast = true
                viewModel.clearPrintEvent()
            }
            is PrintEvent.Failed -> {
                toastMessage = "Printing failed. Bill saved successfully."
                showToast = true
                viewModel.clearPrintEvent()
            }
            else -> {}
        }
    }
    
    LaunchedEffect(showToast) {
        if (showToast) {
            kotlinx.coroutines.delay(3000)
            showToast = false
        }
    }"""

# New dialog UI
replacement = """"""

content = content.replace(target, replacement)

# Now inject the dialog UI at the bottom of the Box
dialog_ui = """
    if (printEvent !is PrintEvent.None) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { if (printEvent !is PrintEvent.Printing) viewModel.clearPrintEvent() },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = printEvent !is PrintEvent.Printing,
                dismissOnClickOutside = printEvent !is PrintEvent.Printing,
                usePlatformDefaultWidth = false
            )
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
                    when (val event = printEvent) {
                        is PrintEvent.Printing -> {
                            androidx.compose.material3.CircularProgressIndicator(color = AppTheme.colors.accent)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Printing...",
                                style = AppTheme.typography.titleLarge,
                                color = AppTheme.colors.textPrimary
                            )
                        }
                        is PrintEvent.Success -> {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Print Successful",
                                style = AppTheme.typography.titleLarge,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            com.example.ui.components.PrimaryButton(
                                text = "OK",
                                onClick = { viewModel.clearPrintEvent() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is PrintEvent.Offline -> {
                            Icon(
                                imageVector = Icons.Rounded.PrintDisabled,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Printer Offline",
                                style = AppTheme.typography.titleLarge,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This order has been saved successfully.\\nYou can reprint it later from Order History.",
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            com.example.ui.components.PrimaryButton(
                                text = "OK",
                                onClick = { viewModel.clearPrintEvent() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is PrintEvent.NoPrinter -> {
                            Icon(
                                imageVector = Icons.Rounded.PrintDisabled,
                                contentDescription = null,
                                tint = AppTheme.colors.textSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Printer Connected",
                                style = AppTheme.typography.titleLarge,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Bill saved. Please connect a printer in settings.",
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            com.example.ui.components.PrimaryButton(
                                text = "OK",
                                onClick = { viewModel.clearPrintEvent() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        is PrintEvent.Failed -> {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Print Failed",
                                style = AppTheme.typography.titleLarge,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Reason: ${event.message}",
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                com.example.ui.components.SecondaryButton(
                                    text = "Cancel",
                                    onClick = { viewModel.clearPrintEvent() },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                com.example.ui.components.PrimaryButton(
                                    text = "OK",
                                    onClick = { viewModel.clearPrintEvent() },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
"""

# Place it before the closing bracket of the root Box
idx = content.rfind("        CustomToast(")
if idx != -1:
    content = content[:idx] + dialog_ui + content[idx:]
else:
    # Just put it before the last closing brace
    last_brace = content.rfind("}")
    content = content[:last_brace] + dialog_ui + content[last_brace:]
    
with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'w') as f:
    f.write(content)
