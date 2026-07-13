with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport kotlinx.coroutines.launch")

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
