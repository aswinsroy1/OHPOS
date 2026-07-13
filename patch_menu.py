import re

with open("app/src/main/java/com/example/ui/screens/MenuScreen.kt", "r") as f:
    content = f.read()

# Add states
states = """
    var showAddItemForm by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<MenuItem?>(null) }
    
    val availableCategories = remember(menuItems) {
        (categories.drop(1) + menuItems.map { it.category }).distinct().filter { it.isNotBlank() }
    }
"""
content = content.replace("var selectedItemForAction by remember { mutableStateOf<MenuItem?>(null) }", states + "\    var selectedItemForAction by remember { mutableStateOf<MenuItem?>(null) }")

# Update isVisible
content = content.replace("isVisible = selectedItemForAction != null,", "isVisible = selectedItemForAction != null || showAddItemForm,")
content = content.replace("onDismissRequest = { selectedItemForAction = null },", "onDismissRequest = { if (selectedItemForAction != null) selectedItemForAction = null else if (showAddItemForm) showAddItemForm = false },")

# Update FAB
fab_click = """onClick = { 
                    itemToEdit = null
                    showAddItemForm = true
                },"""
content = re.sub(r'onClick = \{ /\* TODO: Open add item screen \*/ \},', fab_click, content)

# Update Edit click in ActionPanelItem
edit_click = """onClick = { 
                                    itemToEdit = item
                                    showAddItemForm = true
                                    selectedItemForAction = null
                                }"""
content = content.replace("""onClick = { 
                                    selectedItemForAction = null
                                    // TODO
                                }""", edit_click, 1)

# Update overlayContent
new_overlay = """overlayContent = {
            AnimatedVisibility(
                visible = selectedItemForAction != null,
                enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()),
                exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()),
                modifier = Modifier.align(Alignment.Center)
            ) {
                selectedItemForAction?.let { item ->
                    Box(
                        modifier = Modifier
                            .padding(AppTheme.spacing.lg)
                            .clip(AppTheme.radius.lg)
                            .background(AppTheme.colors.surface)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {}
                            .padding(vertical = AppTheme.spacing.md)
                            .width(260.dp)
                    ) {
                        Column {
                            ActionPanelItem(
                                icon = Icons.Rounded.Edit,
                                text = "Edit Item",
                                onClick = { 
                                    itemToEdit = item
                                    showAddItemForm = true
                                    selectedItemForAction = null
                                }
                            )
                            ActionPanelItem(
                                icon = Icons.Rounded.ContentCopy,
                                text = "Duplicate Item",
                                onClick = { 
                                    val duplicate = item.copy(id = 0, name = "${item.name} (Copy)")
                                    viewModel.saveMenuItem(duplicate)
                                    selectedItemForAction = null
                                }
                            )
                            ActionPanelItem(
                                icon = if (item.isActive) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                text = if (item.isActive) "Temporarily Disable" else "Enable Item",
                                onClick = { 
                                    viewModel.updateMenuAvailability(item.id, !item.isActive)
                                    selectedItemForAction = null
                                }
                            )
                            ActionPanelItem(
                                icon = Icons.Rounded.Delete,
                                text = "Delete Item",
                                textColor = Color(0xFFFF4C4C),
                                iconColor = Color(0xFFFF4C4C),
                                onClick = { 
                                    selectedItemForAction = null
                                    // TODO
                                }
                            )
                        }
                    }
                }
            }
            
            AnimatedVisibility(
                visible = showAddItemForm,
                enter = androidx.compose.animation.slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = PremiumMotion.defaultSpring()
                ),
                exit = androidx.compose.animation.slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = PremiumMotion.defaultSpring()
                ),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(AppTheme.colors.background)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {}
                ) {
                    AddMenuItemForm(
                        initialItem = itemToEdit,
                        existingCategories = availableCategories,
                        onSave = { newItem ->
                            viewModel.saveMenuItem(newItem)
                            showAddItemForm = false
                            itemToEdit = null
                        },
                        onCancel = {
                            showAddItemForm = false
                            itemToEdit = null
                        }
                    )
                }
            }
        }"""

# Replace overlayContent. 
content = re.sub(r'overlayContent = \{.*\}\s*\)\s*\}\s*@Composable', new_overlay + "\n    )\n}\n\n@Composable", content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/MenuScreen.kt", "w") as f:
    f.write(content)
