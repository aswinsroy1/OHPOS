import re

with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'r') as f:
    content = f.read()

# Add states
states_str = """
    val defaultPrinter by viewModel.defaultPrinter.collectAsState()
    val printerStatuses by com.example.util.PrinterStatusMonitor.statuses.collectAsState()
    val defaultPrinterStatus = defaultPrinter?.id?.let { printerStatuses[it] }?.state ?: com.example.util.PrinterState.DISCONNECTED
"""
if "val defaultPrinter by viewModel.defaultPrinter.collectAsState()" not in content:
    content = content.replace("    val printEvent by viewModel.printEvent.collectAsState()",
                              "    val printEvent by viewModel.printEvent.collectAsState()" + states_str)

# Add subtitle to AppTopBar
topbar_regex = r"AppTopBar\(\s*title = \"New Bill\",\s*onMenuClick = onMenuClick,\s*modifier = Modifier\s*\.windowInsetsPadding\(WindowInsets\.statusBars\)\s*\.padding\(horizontal = AppTheme\.spacing\.lg\)\s*\)"

new_topbar = """AppTopBar(
                        title = "New Bill",
                        subtitle = {
                            if (defaultPrinter != null) {
                                val statusColor = when (defaultPrinterStatus) {
                                    com.example.util.PrinterState.CONNECTED -> Color(0xFF4CAF50)
                                    com.example.util.PrinterState.CONNECTING -> Color(0xFFFFC107)
                                    com.example.util.PrinterState.STANDBY -> Color(0xFF9E9E9E)
                                    com.example.util.PrinterState.OFFLINE -> Color(0xFFE53935)
                                    com.example.util.PrinterState.DISCONNECTED -> Color(0xFF424242)
                                }
                                val statusText = when (defaultPrinterStatus) {
                                    com.example.util.PrinterState.CONNECTED -> "Printer Ready"
                                    com.example.util.PrinterState.OFFLINE, com.example.util.PrinterState.DISCONNECTED -> "Printer Offline"
                                    else -> "Connecting..."
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = statusText,
                                        style = AppTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                        color = statusColor
                                    )
                                }
                            }
                        },
                        onMenuClick = onMenuClick,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(horizontal = AppTheme.spacing.lg)
                    )"""

content = re.sub(topbar_regex, new_topbar, content)

with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'w') as f:
    f.write(content)
