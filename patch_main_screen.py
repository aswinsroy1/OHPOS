with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

import_str = "import com.example.util.PinManager\nimport com.example.ui.components.PinEntryDialog\nimport androidx.compose.ui.platform.LocalContext\nimport android.widget.Toast\n"

if "import com.example.util.PinManager" not in content:
    content = content.replace("import com.example.ui.theme.AppTheme", "import com.example.ui.theme.AppTheme\n" + import_str)

pin_states = """    var showDeletionRequests by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var isPinError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val pinManager = remember { PinManager(context) }"""

content = content.replace("    var showDeletionRequests by remember { mutableStateOf(false) }", pin_states)

drawer_click = """                    when (item) {
                        "Billing" -> selectedNavigation = "Billing"
                        "Reports" -> selectedNavigation = "Reports"
                        "Settings" -> selectedNavigation = "Settings"
                        "Deletion Requests" -> {
                            if (pinManager.hasPin()) {
                                showPinDialog = true
                            } else {
                                Toast.makeText(context, "Please set up a PIN in Settings first", Toast.LENGTH_LONG).show()
                            }
                        }
                    }"""

content = content.replace('                    when (item) {\n                        "Billing" -> selectedNavigation = "Billing"\n                        "Reports" -> selectedNavigation = "Reports"\n                        "Settings" -> selectedNavigation = "Settings"\n                        "Deletion Requests" -> showDeletionRequests = true\n                    }', drawer_click)

pin_dialog_ui = """            }
        }

        PinEntryDialog(
            isVisible = showPinDialog,
            title = "Enter PIN",
            subtitle = if (isPinError) "Incorrect PIN" else "Enter PIN to access Deletion Requests",
            isSetupMode = false,
            onPinEntered = { pin ->
                if (pinManager.verifyPin(pin)) {
                    showPinDialog = false
                    isPinError = false
                    showDeletionRequests = true
                } else {
                    isPinError = true
                    // Note: PinEntryDialog handles its own shake error via state internally but we can reset pin input.
                    // To do it correctly, we should pass an error state down.
                }
            },
            onCancel = {
                showPinDialog = false
                isPinError = false
            }
        )
    }
}"""

content = content.replace("            }\n        }\n    }\n}", pin_dialog_ui)

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
