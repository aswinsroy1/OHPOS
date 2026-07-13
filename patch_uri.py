import re

with open("app/src/main/java/com/example/ui/components/AddMenuItemForm.kt", "r") as f:
    content = f.read()

# Add context
content = content.replace("    var showNewCategoryDialog by remember { mutableStateOf(false) }", "    var showNewCategoryDialog by remember { mutableStateOf(false) }\n    val context = androidx.compose.ui.platform.LocalContext.current")

# Update launcher
new_launcher = """    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {}
            imageUri = it.toString()
        }
    }"""
content = re.sub(r'    val photoPickerLauncher = rememberLauncherForActivityResult\([\s\S]*?\}\s*\}', new_launcher, content)

with open("app/src/main/java/com/example/ui/components/AddMenuItemForm.kt", "w") as f:
    f.write(content)
