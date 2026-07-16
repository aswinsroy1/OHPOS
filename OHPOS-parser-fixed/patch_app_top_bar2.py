with open('app/src/main/java/com/example/ui/components/AppTopBar.kt', 'r') as f:
    content = f.read()

old_title = """        if (title != null) {
            androidx.compose.material3.Text(
                text = title,
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary
            )
        }"""

new_title = """        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (title != null) {
                androidx.compose.material3.Text(
                    text = title,
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary
                )
            }
            if (subtitle != null) {
                subtitle()
            }
        }"""

content = content.replace(old_title, new_title)

with open('app/src/main/java/com/example/ui/components/AppTopBar.kt', 'w') as f:
    f.write(content)
