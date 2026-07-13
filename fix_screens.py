import re

def update_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Remove the dynamicBrush and dynamicTextColor logic
    old_logic = """                    val accentColor = AppTheme.colors.accent
                    val isLightAccent = accentColor.luminance() > 0.5f
                    val dynamicTextColor = if (isLightAccent) Color.Black else Color.White
                    val dynamicBrush = androidx.compose.ui.graphics.SolidColor(accentColor)"""
    content = content.replace(old_logic, "")

    # In StatisticsCard calls, remove backgroundBrush and textColor
    content = re.sub(r'(\s*)backgroundBrush\s*=\s*dynamicBrush,?', '', content)
    content = re.sub(r'(\s*)textColor\s*=\s*dynamicTextColor,?', '', content)
    
    # Let's clean up any double commas or stray spaces if needed, but since they were on their own lines, regex should be fine.
    
    with open(filepath, 'w') as f:
        f.write(content)

update_file('app/src/main/java/com/example/ui/screens/HomeScreen.kt')
update_file('app/src/main/java/com/example/ui/screens/ReportsScreen.kt')

print("Updated screens")
