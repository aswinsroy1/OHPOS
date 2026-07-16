with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

if "import android.widget.Toast" not in content:
    content = content.replace("import com.example.ui.screens.PrinterViewModel", "import com.example.ui.screens.PrinterViewModel\nimport android.widget.Toast")

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
