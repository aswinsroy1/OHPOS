def add_optin(filename):
    with open(filename, "r") as f:
        content = f.read()
    content = content.replace("@Composable\nfun PrinterSettingsScreen(", "@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun PrinterSettingsScreen(")
    content = content.replace("@Composable\nfun AddPrinterDialog(", "@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun AddPrinterDialog(")
    content = content.replace("@Composable\nfun SettingsScreen(", "@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun SettingsScreen(")
    with open(filename, "w") as f:
        f.write(content)

add_optin("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt")
add_optin("app/src/main/java/com/example/ui/screens/SettingsScreen.kt")
