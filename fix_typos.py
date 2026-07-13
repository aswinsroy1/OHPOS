import re

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "r") as f:
    content = f.read()

content = content.replace("AppTheme.typography.bodySmall", "AppTheme.typography.bodyMedium")
content = content.replace("AppTheme.typography.labelSmall", "AppTheme.typography.labelMedium")
content = content.replace("AppTheme.colors.error", "Color(0xFFE53935)")

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "w") as f:
    f.write(content)
