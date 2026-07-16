import re

with open("app/src/main/java/com/example/ui/components/AddMenuItemForm.kt", "r") as f:
    content = f.read()

# Add LaunchedEffect after isValid
effect = """    val isValid = name.isNotBlank() && price.toDoubleOrNull()?.let { it >= 0 } == true && category.isNotBlank()
    
    val hasUnsavedChanges = name != (initialItem?.name ?: "") ||
        price != (initialItem?.price?.let { if (it == 0.0) "" else it.toString() } ?: "") ||
        description != (initialItem?.description ?: "") ||
        category != (initialItem?.category ?: "") ||
        imageUri != (initialItem?.imageUrl?.takeIf { it.isNotBlank() })

    LaunchedEffect(hasUnsavedChanges) {
        onHasUnsavedChanges(hasUnsavedChanges)
    }"""
content = content.replace("    val isValid = name.isNotBlank() && price.toDoubleOrNull()?.let { it >= 0 } == true && category.isNotBlank()", effect)

# Update Close button click handler to call onCancel directly and MenuScreen will handle the check
# Wait, onCancel in MenuScreen will close immediately. Instead, let's just make sure onCancel checks if formHasUnsavedChanges is true
# We can do that in MenuScreen.
with open("app/src/main/java/com/example/ui/components/AddMenuItemForm.kt", "w") as f:
    f.write(content)
