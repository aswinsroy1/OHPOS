with open("app/src/main/java/com/example/ui/components/AppTopBar.kt", "r") as f:
    content = f.read()

content = content.replace(
    "    onBackClick: (() -> Unit)? = null,\n    modifier: Modifier = Modifier\n) {",
    "    onBackClick: (() -> Unit)? = null,\n    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.Person,\n    onTrailingClick: () -> Unit = {},\n    trailingContentDescription: String = \"Profile\",\n    modifier: Modifier = Modifier\n) {"
)

content = content.replace(
"""        // Profile picture
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .border(2.dp, AppTheme.colors.divider, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .background(AppTheme.colors.surface)
                .bouncyClickable {},
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Profile",
                tint = AppTheme.colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }""",
"""        // Trailing icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .border(2.dp, AppTheme.colors.divider, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .background(AppTheme.colors.surface)
                .bouncyClickable { onTrailingClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = trailingContentDescription,
                tint = AppTheme.colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }"""
)

with open("app/src/main/java/com/example/ui/components/AppTopBar.kt", "w") as f:
    f.write(content)
