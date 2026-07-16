with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

if "import kotlinx.coroutines.launch" not in content:
    content = content.replace("import kotlinx.coroutines.flow.*", "import kotlinx.coroutines.flow.*\nimport kotlinx.coroutines.launch")

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
