package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.AppTheme
import com.example.data.MenuItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMenuItemForm(
    initialItem: MenuItem?,
    existingCategories: List<String>,
    onSave: (MenuItem) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    onHasUnsavedChanges: (Boolean) -> Unit = {}
) {
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var price by remember { mutableStateOf(initialItem?.price?.toString() ?: "") }
    var deliveryPrice by remember { mutableStateOf(initialItem?.deliveryPrice?.toString() ?: "") }
    var description by remember { mutableStateOf(initialItem?.description ?: "") }
    var category by remember { mutableStateOf(initialItem?.category ?: "") }
    var imageUri by remember { mutableStateOf<String?>(initialItem?.imageUrl?.takeIf { it.isNotBlank() }) }

    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                if (inputStream != null) {
                    val file = java.io.File(context.filesDir, "menu_item_${java.util.UUID.randomUUID()}.jpg")
                    val outputStream = java.io.FileOutputStream(file)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    imageUri = Uri.fromFile(file).toString()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val isValid = name.isNotBlank() && price.toDoubleOrNull()?.let { it >= 0 } == true && category.isNotBlank()
    
    val hasUnsavedChanges = name != (initialItem?.name ?: "") ||
        price != (initialItem?.price?.let { if (it == 0.0) "" else it.toString() } ?: "") ||
        deliveryPrice != (initialItem?.deliveryPrice?.toString() ?: "") ||
        description != (initialItem?.description ?: "") ||
        category != (initialItem?.category ?: "") ||
        imageUri != (initialItem?.imageUrl?.takeIf { it.isNotBlank() })

    LaunchedEffect(hasUnsavedChanges) {
        onHasUnsavedChanges(hasUnsavedChanges)
    }

    Box(modifier = modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing.lg)
                .windowInsetsPadding(WindowInsets.statusBars),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(AppTheme.radius.md)
                    .background(AppTheme.colors.surface)
                    .bouncyClickable { onCancel() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = AppTheme.colors.textPrimary
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = if (initialItem != null) "Edit Item" else "Add Menu Item",
                style = AppTheme.typography.titleLarge,
                color = AppTheme.colors.textPrimary
            )
            
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(AppTheme.radius.md)
                    .background(if (isValid) AppTheme.colors.accent else AppTheme.colors.surface)
                    .then(if (isValid) Modifier.bouncyClickable {
                        val parsedPrice = price.toDoubleOrNull() ?: 0.0
                        val parsedDeliveryPrice = deliveryPrice.toDoubleOrNull()
                        val newItem = MenuItem(
                            id = initialItem?.id ?: 0,
                            name = name.trim(),
                            category = category.trim(),
                            price = parsedPrice,
                            deliveryPrice = parsedDeliveryPrice,
                            description = description.trim(),
                            imageUrl = imageUri ?: "",
                            isActive = initialItem?.isActive ?: true // Ensure new items are active
                        )
                        onSave(newItem)
                    } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Save",
                    tint = if (isValid) AppTheme.colors.background else AppTheme.colors.textSecondary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = AppTheme.spacing.lg),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Picker
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .clip(AppTheme.radius.lg)
                                .background(AppTheme.colors.surface)
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.1f),
                                    shape = AppTheme.radius.lg
                                )
                                .bouncyClickable {
                                    photoPickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageUri != null) {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Menu Item Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Small edit button
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(AppTheme.colors.accent.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = null,
                                            tint = AppTheme.colors.accent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Add Image", style = AppTheme.typography.labelMedium, color = AppTheme.colors.textPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("JPG, PNG up to 5MB", style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textSecondary, textAlign = TextAlign.Center)
                                }
                            }
                        }
                        
                        if (imageUri != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { imageUri = null }) {
                                Text("Remove Image", color = Color(0xFFFF4C4C), style = AppTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            item {
                PremiumTextField(
                    value = name,
                    onValueChange = { if (it.length <= 80) name = it },
                    placeholder = "e.g. Margherita Pizza",
                    icon = Icons.Rounded.ShoppingBag,
                    label = "Item Name"
                )
            }

            item {
                Box {
                    PremiumTextField(
                        value = category,
                        onValueChange = {}, // Read only
                        placeholder = "Select category",
                        icon = Icons.Rounded.Assignment,
                        label = "Category",
                        trailingIcon = {
                            Icon(
                                imageVector = if (showCategoryDropdown) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AppTheme.colors.textSecondary
                            )
                        },
                        modifier = Modifier.bouncyClickable { showCategoryDropdown = true }
                    )
                    
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier.background(AppTheme.colors.surface)
                    ) {
                        existingCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = AppTheme.colors.textPrimary) },
                                onClick = {
                                    category = cat
                                    showCategoryDropdown = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("+ Create New Category", color = AppTheme.colors.accent) },
                            onClick = {
                                showCategoryDropdown = false
                                showNewCategoryDialog = true
                            }
                        )
                    }
                }
            }

            item {
                PremiumTextField(
                    value = price,
                    onValueChange = { newValue -> 
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            price = newValue
                        }
                    },
                    placeholder = "₹ 0.00",
                    icon = Icons.Rounded.CurrencyRupee,
                    label = "Restaurant Price *",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            
            item {
                PremiumTextField(
                    value = deliveryPrice,
                    onValueChange = { newValue -> 
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            deliveryPrice = newValue
                        }
                    },
                    placeholder = "₹ 0.00",
                    icon = Icons.Rounded.CurrencyRupee,
                    label = "Online / Delivery Price (Optional)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            item {
                PremiumTextField(
                    value = description,
                    onValueChange = { if (it.length <= 250) description = it },
                    placeholder = "e.g. Classic pizza with tomato sauce and mozzarella.",
                    icon = Icons.Rounded.Description,
                    label = "Description",
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.heightIn(min = 120.dp)
                )
            }
        }
    }



    if (showNewCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showNewCategoryDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.spacing.lg)
                    .clip(AppTheme.radius.xl)
                    .background(AppTheme.colors.surface)
                    .border(1.dp, AppTheme.colors.borderLight, AppTheme.radius.xl)
                    .padding(AppTheme.spacing.xl)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "New Category",
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                    
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        placeholder = { Text("e.g. Beverages") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppTheme.colors.textPrimary,
                            unfocusedTextColor = AppTheme.colors.textPrimary,
                            cursorColor = AppTheme.colors.accent,
                            focusedBorderColor = AppTheme.colors.accent,
                            unfocusedBorderColor = AppTheme.colors.textSecondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    
                    PrimaryButton(
                        text = "Add",
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                category = newCategoryName.trim()
                                showNewCategoryDialog = false
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(AppTheme.radius.md)
                            .liftClickable { showNewCategoryDialog = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            style = AppTheme.typography.titleMedium,
                            color = AppTheme.colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}


}
