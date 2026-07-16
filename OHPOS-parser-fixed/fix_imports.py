import re

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "r") as f:
    content = f.read()

content = content.replace("import com.example.data.PrinterPreferencesRepositoryButton", "import androidx.compose.material3.TextButton")
content = content.replace("context(context)", "context")

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "w") as f:
    f.write(content)
