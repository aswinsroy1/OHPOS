with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "r") as f:
    content = f.read()

# Add proper containerColor and shape to both ModalBottomSheet in AddPrinterDialog
import re

content = content.replace("androidx.compose.material3.ModalBottomSheet(\n            onDismissRequest = onDismissRequest\n        ) {", 
"""androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            containerColor = AppTheme.colors.surface,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {""")

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "w") as f:
    f.write(content)
