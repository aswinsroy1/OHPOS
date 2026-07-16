with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "r") as f:
    content = f.read()

content = content.replace("AppTheme.typography.bodySmall", "AppTheme.typography.bodyMedium")

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "w") as f:
    f.write(content)
