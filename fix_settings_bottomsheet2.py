import re

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

# Replace showPaperSizeSheet block using simple string replace
old_paper = """        if (showPaperSizeSheet) {
            com.example.ui.components.PremiumBottomSheet(
                onDismissRequest = { showPaperSizeSheet = false },
                title = "Select Paper Size",
                subtitle = "Changing paper size adjusts receipt formatting automatically."
            ) {"""
new_paper = """        if (showPaperSizeSheet) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showPaperSizeSheet = false }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md)) {
                    Text("Select Paper Size", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
                    Text("Changing paper size adjusts receipt formatting automatically.", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                    Spacer(modifier = Modifier.height(16.dp))"""
content = content.replace(old_paper, new_paper)

# Replace showTestPrintSheet block
old_test = """        if (showTestPrintSheet) {
            com.example.ui.components.PremiumBottomSheet(
                onDismissRequest = { showTestPrintSheet = false },
                title = "Select Printer",
                subtitle = "Choose a printer for the test print"
            ) {"""
new_test = """        if (showTestPrintSheet) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showTestPrintSheet = false }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md)) {
                    Text("Select Printer", style = AppTheme.typography.titleLarge, color = AppTheme.colors.textPrimary)
                    Text("Choose a printer for the test print", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary)
                    Spacer(modifier = Modifier.height(16.dp))"""
content = content.replace(old_test, new_test)

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
