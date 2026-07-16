import re

with open("app/src/main/java/com/example/ui/screens/MenuScreen.kt", "r") as f:
    content = f.read()

# Add states
states = """    var showSuccessToast by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<MenuItem?>(null) }
    var showDeleteToast by remember { mutableStateOf(false) }
    
    androidx.compose.runtime.LaunchedEffect(showDeleteToast) {
        if (showDeleteToast) {
            kotlinx.coroutines.delay(2000)
            showDeleteToast = false
        }
    }"""
content = content.replace("    var showSuccessToast by remember { mutableStateOf(false) }", states)

# Add toast UI
delete_toast = """
                // Delete Toast
                AnimatedVisibility(
                    visible = showDeleteToast,
                    enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + androidx.compose.animation.slideInVertically(initialOffsetY = { -it }),
                    exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = dynamicTopPadding + 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(AppTheme.radius.full)
                            .background(AppTheme.colors.surface)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), AppTheme.radius.full)
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text("Menu item deleted", style = AppTheme.typography.labelMedium, color = AppTheme.colors.textPrimary)
                    }
                }
"""
content = re.sub(r'(\s*)// FAB', delete_toast + r'\1// FAB', content)

# Update Delete action
delete_action = """onClick = { 
                                    itemToDelete = item
                                    selectedItemForAction = null
                                }"""
content = content.replace("""onClick = { 
                                    selectedItemForAction = null
                                    // TODO
                                }""", delete_action)

# Add Delete Dialog
delete_dialog = """
    if (itemToDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { androidx.compose.material3.Text("Delete Item?", color = AppTheme.colors.textPrimary) },
            text = { androidx.compose.material3.Text("This action cannot be undone.", color = AppTheme.colors.textSecondary) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        itemToDelete?.let {
                            viewModel.deleteMenuItem(it.id)
                            showDeleteToast = true
                        }
                        itemToDelete = null
                    }
                ) {
                    androidx.compose.material3.Text("Delete", color = androidx.compose.ui.graphics.Color(0xFFFF4C4C))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { itemToDelete = null }) {
                    androidx.compose.material3.Text("Cancel", color = AppTheme.colors.textPrimary)
                }
            },
            containerColor = AppTheme.colors.surface
        )
    }
}"""
content = re.sub(r'containerColor = AppTheme\.colors\.surface\s*\)\s*\}\s*\}', 'containerColor = AppTheme.colors.surface\n        )\n    }' + delete_dialog, content)

with open("app/src/main/java/com/example/ui/screens/MenuScreen.kt", "w") as f:
    f.write(content)

