import re

with open("app/src/main/java/com/example/ui/screens/MenuScreen.kt", "r") as f:
    content = f.read()

# Add states
states = """    var showAddItemForm by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<MenuItem?>(null) }
    var formHasUnsavedChanges by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }"""
content = re.sub(r'    var showAddItemForm by remember \{ mutableStateOf\(false\) \}\s*var itemToEdit by remember \{ mutableStateOf<MenuItem\?>\(null\) \}', states, content)

# Update onDismissRequest
dismiss = """onDismissRequest = { 
            if (selectedItemForAction != null) {
                selectedItemForAction = null 
            } else if (showAddItemForm) {
                if (formHasUnsavedChanges) showUnsavedDialog = true
                else showAddItemForm = false
            }
        },"""
content = re.sub(r'onDismissRequest = \{ if \(selectedItemForAction != null\) selectedItemForAction = null else if \(showAddItemForm\) showAddItemForm = false \},', dismiss, content)

# Update AddMenuItemForm call
form = """AddMenuItemForm(
                        initialItem = itemToEdit,
                        existingCategories = availableCategories,
                        onHasUnsavedChanges = { formHasUnsavedChanges = it },
                        onSave = { newItem ->
                            viewModel.saveMenuItem(newItem)
                            showAddItemForm = false
                            itemToEdit = null
                            formHasUnsavedChanges = false
                        },
                        onCancel = {
                            if (formHasUnsavedChanges) showUnsavedDialog = true
                            else {
                                showAddItemForm = false
                                itemToEdit = null
                            }
                        }
                    )"""
content = re.sub(r'AddMenuItemForm\([\s\S]*?onCancel = \{[\s\S]*?\}\s*\)', form, content)

# Add UnsavedDialog
dialog = """
    if (showUnsavedDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { androidx.compose.material3.Text("Discard Changes?", color = AppTheme.colors.textPrimary) },
            text = { androidx.compose.material3.Text("You have unsaved changes. Are you sure you want to discard them?", color = AppTheme.colors.textSecondary) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showUnsavedDialog = false
                        showAddItemForm = false
                        itemToEdit = null
                        formHasUnsavedChanges = false
                    }
                ) {
                    androidx.compose.material3.Text("Discard", color = androidx.compose.ui.graphics.Color(0xFFFF4C4C))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showUnsavedDialog = false }) {
                    androidx.compose.material3.Text("Continue Editing", color = AppTheme.colors.textPrimary)
                }
            },
            containerColor = AppTheme.colors.surface
        )
    }
}"""
content = re.sub(r'\}\n\n@Composable\nprivate fun ActionPanelItem', dialog + "\n\n@Composable\nprivate fun ActionPanelItem", content)

with open("app/src/main/java/com/example/ui/screens/MenuScreen.kt", "w") as f:
    f.write(content)
