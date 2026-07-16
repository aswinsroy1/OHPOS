import re

with open('app/src/main/java/com/example/ui/components/StatisticsCard.kt', 'r') as f:
    content = f.read()

# Replace the signature of StatisticsCard
old_sig = """fun StatisticsCard(
    title: String,
    value: String,
    subtitle: String,
    backTitle: String = "",
    backValue: String = "",
    backSubtitle: String = "",
    icon: ImageVector,
    backgroundBrush: Brush,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
)"""

new_sig = """fun StatisticsCard(
    title: String,
    value: String,
    subtitle: String,
    backTitle: String = "",
    backValue: String = "",
    backSubtitle: String = "",
    icon: ImageVector,
    backgroundBrush: Brush = androidx.compose.ui.graphics.SolidColor(AppTheme.colors.surfaceLighter),
    textColor: Color = AppTheme.colors.textPrimary,
    badgeColor: Color = AppTheme.colors.accent,
    badgeIconColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
)"""

content = content.replace(old_sig, new_sig)

# Replace the badge color logic inside the component
# Finding: .background(androidx.compose.ui.graphics.SolidColor(textColor.copy(alpha = 0.15f)))
content = content.replace('.background(androidx.compose.ui.graphics.SolidColor(textColor.copy(alpha = 0.15f)))', 
                          '.background(androidx.compose.ui.graphics.SolidColor(badgeColor))')

# Finding: color = textColor.copy(alpha = 0.2f),
content = content.replace('color = textColor.copy(alpha = 0.2f),',
                          'color = badgeColor.copy(alpha = 0.2f),')

# Replacing Icon tint:
# tint = textColor
old_icon = """                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(AppTheme.dimensions.iconSizeSm)
                )"""

new_icon = """                val resolvedIconColor = if (badgeIconColor != Color.Unspecified) badgeIconColor else {
                    val isMonochrome = badgeColor == AppTheme.colors.textPrimary
                    if (isMonochrome) AppTheme.colors.background else (if (badgeColor.luminance() > 0.5f) Color.Black else Color.White)
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = resolvedIconColor,
                    modifier = Modifier.size(AppTheme.dimensions.iconSizeSm)
                )"""

content = content.replace(old_icon, new_icon)

# Add luminance import if missing
if 'import com.example.ui.util.luminance' not in content and 'import androidx.compose.ui.graphics.luminance' not in content:
    content = content.replace('import androidx.compose.ui.graphics.Color', 'import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.graphics.luminance')

with open('app/src/main/java/com/example/ui/components/StatisticsCard.kt', 'w') as f:
    f.write(content)
