package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MenuItem
import com.example.ui.theme.AppTheme

@Composable
fun PdfImportReviewOverlay(
    isVisible: Boolean,
    parsedItems: List<MenuItem>,
    existingItems: List<MenuItem>,
    onDismissRequest: () -> Unit,
    onItemChange: (Int, MenuItem) -> Unit,
    onImportConfirmed: (List<ImportAction>) -> Unit
) {
    if (!isVisible) return

    val context = androidx.compose.ui.platform.LocalContext.current
    val appearanceRepo = androidx.compose.runtime.remember { com.example.data.AppearancePreferencesRepository(context) }
    val themePref by appearanceRepo.themeFlow.collectAsState(initial = "Dark")
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDarkTheme = when (themePref) {
        "Light" -> false
        "System Default" -> isSystemDark
        else -> true // Dark
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val dialogWindowProvider = androidx.compose.ui.platform.LocalView.current.parent as? androidx.compose.ui.window.DialogWindowProvider
        val window = dialogWindowProvider?.window
        
        DisposableEffect(window, isDarkTheme) {
            window?.setDimAmount(0f)
            window?.let {
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(it, it.decorView)
                insetsController.isAppearanceLightStatusBars = !isDarkTheme
                insetsController.isAppearanceLightNavigationBars = !isDarkTheme
                it.statusBarColor = android.graphics.Color.TRANSPARENT
                it.navigationBarColor = android.graphics.Color.TRANSPARENT
            }
            onDispose {}
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppTheme.spacing.lg)
                .padding(top = 48.dp, bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        ) {
            Text(
                text = "Review Import",
                style = AppTheme.typography.titleLarge,
                color = AppTheme.colors.textPrimary
            )
            Text(
                text = "Edit detected items before importing",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
                modifier = Modifier.padding(bottom = AppTheme.spacing.lg)
            )

            val actionMap = remember(parsedItems) { mutableStateMapOf<Int, ImportActionType>() }
            
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                itemsIndexed(parsedItems) { index, item ->
                    val isDuplicate = existingItems.any { it.name.equals(item.name, ignoreCase = true) }
                    PdfReviewItemCard(
                        item = item,
                        isDuplicate = isDuplicate,
                        chosenAction = actionMap[index] ?: (if (isDuplicate) ImportActionType.SKIP else ImportActionType.IMPORT),
                        onActionChanged = { actionMap[index] = it },
                        onItemChange = { onItemChange(index, it) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = AppTheme.radius.full,
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.surface)
                ) {
                    Text("Cancel", color = AppTheme.colors.textPrimary)
                }
                
                Button(
                    onClick = {
                        val actions = parsedItems.mapIndexed { index, item ->
                            val isDuplicate = existingItems.any { it.name.equals(item.name, ignoreCase = true) }
                            val actionType = actionMap[index] ?: (if (isDuplicate) ImportActionType.SKIP else ImportActionType.IMPORT)
                            val existingId = if (isDuplicate) existingItems.first { it.name.equals(item.name, ignoreCase = true) }.id else 0
                            ImportAction(item, actionType, existingId)
                        }
                        onImportConfirmed(actions)
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = AppTheme.radius.full,
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accent)
                ) {
                    Text("Import", color = AppTheme.colors.background)
                }
            }
        }
    }
    }
}

enum class ImportActionType { SKIP, REPLACE, IMPORT }

data class ImportAction(
    val item: MenuItem,
    val type: ImportActionType,
    val existingId: Int = 0
)

@Composable
fun PdfReviewItemCard(
    item: MenuItem,
    isDuplicate: Boolean,
    chosenAction: ImportActionType,
    onActionChanged: (ImportActionType) -> Unit,
    onItemChange: (MenuItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppTheme.radius.lg)
            .background(AppTheme.colors.surface)
            .border(1.dp, Color.White.copy(alpha = 0.05f), AppTheme.radius.lg)
            .padding(AppTheme.spacing.md)
    ) {
        if (isDuplicate) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(AppTheme.radius.full)
                        .background(Color(0xFFE5A443).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = Color(0xFFE5A443),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Already Exists",
                        style = AppTheme.typography.labelMedium.copy(fontSize = 11.sp),
                        color = Color(0xFFE5A443)
                    )
                }
                
                var expanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { expanded = true }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp), modifier = Modifier.height(28.dp)) {
                        Text(chosenAction.name, style = AppTheme.typography.labelMedium, color = AppTheme.colors.accent)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(AppTheme.colors.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Skip", color = AppTheme.colors.textPrimary) },
                            onClick = { onActionChanged(ImportActionType.SKIP); expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Replace Existing", color = AppTheme.colors.textPrimary) },
                            onClick = { onActionChanged(ImportActionType.REPLACE); expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Import Anyway", color = AppTheme.colors.textPrimary) },
                            onClick = { onActionChanged(ImportActionType.IMPORT); expanded = false }
                        )
                    }
                }
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Name", style = AppTheme.typography.labelMedium, color = AppTheme.colors.textSecondary)
                EditableField(value = item.name, onValueChange = { onItemChange(item.copy(name = it)) })
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Category", style = AppTheme.typography.labelMedium, color = AppTheme.colors.textSecondary)
                EditableField(value = item.category, onValueChange = { onItemChange(item.copy(category = it)) })
            }
        }
        
        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
        
        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Restaurant Price", style = AppTheme.typography.labelMedium, color = AppTheme.colors.textSecondary)
                EditableField(
                    value = if (item.price == 0.0) "" else if (item.price % 1.0 == 0.0) item.price.toLong().toString() else item.price.toString(),
                    onValueChange = { onItemChange(item.copy(price = it.toDoubleOrNull() ?: 0.0)) },
                    keyboardType = KeyboardType.Number
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Online Price (Optional)", style = AppTheme.typography.labelMedium, color = AppTheme.colors.textSecondary)
                EditableField(
                    value = item.deliveryPrice?.let { if (it == 0.0) "" else if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "",
                    onValueChange = { onItemChange(item.copy(deliveryPrice = it.toDoubleOrNull())) },
                    keyboardType = KeyboardType.Number
                )
            }
        }
    }
}

@Composable
fun EditableField(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.textPrimary),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        cursorBrush = SolidColor(AppTheme.colors.accent),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color.White.copy(alpha = 0.05f), AppTheme.radius.sm)
            .border(1.dp, Color.White.copy(alpha = 0.1f), AppTheme.radius.sm)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    )
}
