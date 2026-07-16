with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

bad_column = "                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md)) {\n"

# Both showPaperSizeSheet and showTestPrintSheet have it duplicated.
# We will just remove the second occurrence which follows Spacer
content = content.replace(
    'Spacer(modifier = Modifier.height(16.dp))\n' + bad_column,
    'Spacer(modifier = Modifier.height(16.dp))\n'
)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
