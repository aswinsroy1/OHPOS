with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

import_pin = "import com.example.util.PinManager\nimport com.example.ui.components.PinEntryDialog\nimport androidx.compose.ui.platform.LocalContext\nimport androidx.compose.material.icons.rounded.Lock"

if "import com.example.util.PinManager" not in content:
    content = content.replace("import com.example.ui.theme.AppTheme", "import com.example.ui.theme.AppTheme\n" + import_pin)

pin_setup_state = """    var searchQuery by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val pinManager = remember { PinManager(context) }
    var showPinSetup by remember { mutableStateOf(false) }"""

content = content.replace("    var searchQuery by remember { mutableStateOf(\"\") }", pin_setup_state)

security_section = """        SettingSectionData(
            title = "Security",
            items = listOf(
                SettingItemData(if (pinManager.hasPin()) "Update Deletion PIN" else "Setup Deletion PIN", Icons.Rounded.Lock, onClick = { showPinSetup = true })
            )
        ),"""

content = content.replace("        val allSections = listOf(", "        val allSections = listOf(\n" + security_section)

pin_dialog_ui = """            }
        }
        
        PinEntryDialog(
            isVisible = showPinSetup,
            title = if (pinManager.hasPin()) "Update PIN" else "Setup PIN",
            subtitle = if (pinManager.hasPin()) "Enter new 4-digit PIN" else "Create a 4-digit PIN for Deletion Approvals",
            isSetupMode = true,
            onPinEntered = { newPin ->
                pinManager.setPin(newPin)
                showPinSetup = false
            },
            onCancel = { showPinSetup = false }
        )
    }
}"""

content = content.replace("            }\n        }\n    }\n}", pin_dialog_ui)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)

