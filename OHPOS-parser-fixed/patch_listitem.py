with open("app/src/main/java/com/example/ui/components/ListItem.kt", "r") as f:
    content = f.read()

import_str = "import androidx.compose.ui.draw.alpha\nimport androidx.compose.material.icons.rounded.DeleteOutline\nimport androidx.compose.material.icons.Icons"
if "import androidx.compose.ui.draw.alpha" not in content:
    content = content.replace("import androidx.compose.ui.draw.shadow", "import androidx.compose.ui.draw.shadow\n" + import_str)

content = content.replace(
    "    onLongPress: (() -> Unit)? = null\n) {",
    "    onLongPress: (() -> Unit)? = null,\n    isPendingDeletion: Boolean = false\n) {"
)

content = content.replace(
    "    ) {\n        Row(",
    "    ).alpha(if (isPendingDeletion) 0.65f else 1f)\n    ) {\n        Row("
)

title_row = """                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colors.textPrimary.copy(alpha = 0.9f)
                    )
                    if (isPendingDeletion) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Pending Deletion",
                            tint = AppTheme.colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }"""

content = content.replace(
"""                Text(
                    text = title,
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.textPrimary.copy(alpha = 0.9f)
                )""", title_row)

pending_badge = """                    if (isPendingDeletion) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(AppTheme.radius.sm)
                                .background(AppTheme.colors.textSecondary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Pending Deletion",
                                style = AppTheme.typography.labelMedium,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                    }"""

content = content.replace(
"""                    if (statusText != null && statusColor != null) {
                        Spacer(modifier = Modifier.width(8.dp))""",
pending_badge + """
                    if (statusText != null && statusColor != null) {
                        Spacer(modifier = Modifier.width(8.dp))"""
)

with open("app/src/main/java/com/example/ui/components/ListItem.kt", "w") as f:
    f.write(content)
