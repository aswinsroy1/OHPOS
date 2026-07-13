import re

with open("app/src/main/java/com/example/ui/screens/MenuScreen.kt", "r") as f:
    content = f.read()

# Add states
states = """    var showSuccessToast by remember { mutableStateOf(false) }
    
    androidx.compose.runtime.LaunchedEffect(showSuccessToast) {
        if (showSuccessToast) {
            kotlinx.coroutines.delay(2000)
            showSuccessToast = false
        }
    }"""
content = re.sub(r'    var showUnsavedDialog by remember \{ mutableStateOf\(false\) \}', "    var showUnsavedDialog by remember { mutableStateOf(false) }\n" + states, content)


# Add toast to onSave
save_click = """onSave = { newItem ->
                            viewModel.saveMenuItem(newItem)
                            showAddItemForm = false
                            itemToEdit = null
                            formHasUnsavedChanges = false
                            if (newItem.id == 0) showSuccessToast = true
                        },"""
content = re.sub(r'onSave = \{ newItem ->.*?formHasUnsavedChanges = false\n\s*\},', save_click, content, flags=re.DOTALL)

# Add toast UI at the end of the top Box(modifier = Modifier.fillMaxSize()) in PremiumModalOverlay content
toast_ui = """
                // Success Toast
                AnimatedVisibility(
                    visible = showSuccessToast,
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
                        Text("Menu item added", style = AppTheme.typography.labelLarge, color = AppTheme.colors.textPrimary)
                    }
                }
"""

content = re.sub(r'(\s*)// FAB', toast_ui + r'\1// FAB', content)


with open("app/src/main/java/com/example/ui/screens/MenuScreen.kt", "w") as f:
    f.write(content)
