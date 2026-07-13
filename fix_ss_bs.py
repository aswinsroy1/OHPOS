with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

replacement1 = """androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showPaperSizeSheet = false },
                containerColor = AppTheme.colors.surface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )"""
content = content.replace("androidx.compose.material3.ModalBottomSheet(\n                onDismissRequest = { showPaperSizeSheet = false }\n            )", replacement1)

replacement2 = """androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showTestPrintSheet = false },
                containerColor = AppTheme.colors.surface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )"""
content = content.replace("androidx.compose.material3.ModalBottomSheet(\n                onDismissRequest = { showTestPrintSheet = false }\n            )", replacement2)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
