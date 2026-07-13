import re

with open('app/src/main/java/com/example/ui/components/PrimaryButton.kt', 'r') as f:
    content = f.read()

target = """@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = AppTheme.colors.accent,
    contentColor: androidx.compose.ui.graphics.Color = AppTheme.colors.background
) {"""

replacement = """@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = AppTheme.colors.accent,
    contentColor: androidx.compose.ui.graphics.Color = AppTheme.colors.background,
    enabled: Boolean = true
) {"""

content = content.replace(target, replacement)

target2 = """            .background(containerColor)
            .bouncyClickable(onClick = onClick),"""

replacement2 = """            .background(if (enabled) containerColor else containerColor.copy(alpha = 0.5f))
            .then(if (enabled) Modifier.bouncyClickable(onClick = onClick) else Modifier),"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/components/PrimaryButton.kt', 'w') as f:
    f.write(content)

