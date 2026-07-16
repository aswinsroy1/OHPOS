package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.RestaurantProfile
import com.example.ui.components.*
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsState()
    
    var showEditProfile by remember { mutableStateOf(false) }
    var showSuccessToast by remember { mutableStateOf(false) }
    
    LaunchedEffect(showSuccessToast) {
        if (showSuccessToast) {
            delay(2000)
            showSuccessToast = false
        }
    }

    BackHandler {
        if (showEditProfile) {
            showEditProfile = false
        } else {
            onBackClick()
        }
    }

    PremiumModalOverlay(
        isVisible = showEditProfile,
        onDismissRequest = { showEditProfile = false },
        content = {
            AppScaffold(
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppTheme.colors.background)
                    ) {
                        AppTopBar(
                            title = "Profile",
                            onBackClick = onBackClick,
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .padding(horizontal = AppTheme.spacing.lg)
                        )
                    }
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 100.dp, bottom = 120.dp)
                        .padding(horizontal = AppTheme.spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Header Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppTheme.radius.lg)
                            .background(AppTheme.colors.surface)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), AppTheme.radius.lg)
                            .padding(AppTheme.spacing.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(AppTheme.colors.accent.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!profile?.logoUri.isNullOrBlank()) {
                                        AsyncImage(
                                            model = profile?.logoUri,
                                            contentDescription = "Restaurant Logo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Rounded.Restaurant,
                                            contentDescription = null,
                                            tint = AppTheme.colors.accent,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 4.dp, y = 4.dp)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(AppTheme.colors.accent)
                                        .clickable { showEditProfile = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                            
                            Text(
                                text = profile?.name?.takeIf { it.isNotBlank() } ?: "Restaurant Name",
                                style = AppTheme.typography.titleLarge,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = profile?.businessType?.takeIf { it.isNotBlank() } ?: "Not configured",
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    
                    // Business Information Cards
                    ProfileSettingCard(
                        icon = Icons.Rounded.Phone,
                        title = "Contact Number",
                        value = profile?.phone?.takeIf { it.isNotBlank() } ?: "Not configured",
                        onClick = { showEditProfile = true }
                    )
                    ProfileSettingCard(
                        icon = Icons.Rounded.Email,
                        title = "Email Address",
                        value = profile?.email?.takeIf { it.isNotBlank() } ?: "Not configured",
                        onClick = { showEditProfile = true }
                    )
                    ProfileSettingCard(
                        icon = Icons.Rounded.LocationOn,
                        title = "Restaurant Address",
                        value = profile?.address?.takeIf { it.isNotBlank() } ?: "Not configured",
                        onClick = { showEditProfile = true }
                    )
                    ProfileSettingCard(
                        icon = Icons.Rounded.AccessTime,
                        title = "Business Hours",
                        value = if (!profile?.openingTime.isNullOrBlank() && !profile?.closingTime.isNullOrBlank()) {
                            "${profile?.openingTime} - ${profile?.closingTime}"
                        } else "Not configured",
                        onClick = { showEditProfile = true }
                    )
                    ProfileSettingCard(
                        icon = Icons.Rounded.ReceiptLong,
                        title = "Tax ID / GST",
                        value = profile?.gstNumber?.takeIf { it.isNotBlank() } ?: "Not configured",
                        onClick = { showEditProfile = true }
                    )
                    
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    
                    PrimaryButton(
                        text = "Edit Profile",
                        onClick = { showEditProfile = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Success Toast
                AnimatedVisibility(
                    visible = showSuccessToast,
                    enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + slideInVertically(initialOffsetY = { -it }),
                    exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + slideOutVertically(targetOffsetY = { -it }),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(AppTheme.radius.full)
                            .background(AppTheme.colors.surface)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), AppTheme.radius.full)
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text("Profile updated successfully", style = AppTheme.typography.labelMedium, color = AppTheme.colors.textPrimary)
                    }
                }
                }
            }
        },
        overlayContent = {
            AnimatedVisibility(
                visible = showEditProfile,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = PremiumMotion.defaultSpring()
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = PremiumMotion.defaultSpring()
                ),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(AppTheme.colors.background)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {}
                ) {
                    EditProfileForm(
                        initialProfile = profile,
                        onSave = { updatedProfile ->
                            viewModel.saveProfile(updatedProfile)
                            showEditProfile = false
                            showSuccessToast = true
                        },
                        onCancel = { showEditProfile = false }
                    )
                }
            }
        }
    )
}

@Composable
private fun ProfileSettingCard(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppTheme.spacing.md)
            .clip(AppTheme.radius.lg)
            .background(AppTheme.colors.surface)
            .border(1.dp, Color.White.copy(alpha = 0.05f), AppTheme.radius.lg)
            .bouncyClickable { onClick() }
            .padding(AppTheme.spacing.lg)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppTheme.colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(AppTheme.spacing.lg))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTheme.typography.labelMedium,
                    color = AppTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.textPrimary
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = AppTheme.colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun EditProfileForm(
    initialProfile: RestaurantProfile?,
    onSave: (RestaurantProfile) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initialProfile?.name ?: "") }
    var businessType by remember { mutableStateOf(initialProfile?.businessType ?: "") }
    var phone by remember { mutableStateOf(initialProfile?.phone ?: "") }
    var email by remember { mutableStateOf(initialProfile?.email ?: "") }
    var address by remember { mutableStateOf(initialProfile?.address ?: "") }
    var gstNumber by remember { mutableStateOf(initialProfile?.gstNumber ?: "") }
    var openingTime by remember { mutableStateOf(initialProfile?.openingTime ?: "") }
    var closingTime by remember { mutableStateOf(initialProfile?.closingTime ?: "") }
    var logoUri by remember { mutableStateOf(initialProfile?.logoUri) }
    
    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val file = File(context.filesDir, "logo_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            logoUri = Uri.fromFile(file).toString()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Cancel",
                    tint = AppTheme.colors.textPrimary
                )
            }
            Text(
                text = "Edit Profile",
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary
            )
            TextButton(
                onClick = {
                    onSave(
                        RestaurantProfile(
                            id = 1,
                            name = name,
                            businessType = businessType,
                            phone = phone,
                            email = email,
                            address = address,
                            gstNumber = gstNumber,
                            openingTime = openingTime,
                            closingTime = closingTime,
                            logoUri = logoUri
                        )
                    )
                }
            ) {
                Text(
                    text = "Save",
                    style = AppTheme.typography.labelMedium,
                    color = AppTheme.colors.accent
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppTheme.spacing.lg),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.surface)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                            .clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoUri != null) {
                            AsyncImage(
                                model = logoUri,
                                contentDescription = "Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.AddAPhoto,
                                contentDescription = "Add Photo",
                                tint = AppTheme.colors.textSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
            }
            
            item {
                PremiumTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Restaurant Name",
                    placeholder = "",
                    icon = Icons.Rounded.Restaurant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
            }
            
            item {
                PremiumTextField(
                    value = businessType,
                    onValueChange = { businessType = it },
                    label = "Business Type",
                    placeholder = "e.g. Fine Dining Restaurant",
                    icon = Icons.Rounded.Business,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
            }
            
            item {
                PremiumTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Contact Number",
                    placeholder = "",
                    icon = Icons.Rounded.Phone,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
            }
            
            item {
                PremiumTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address",
                    placeholder = "",
                    icon = Icons.Rounded.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
            }
            
            item {
                PremiumTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Restaurant Address",
                    placeholder = "",
                    icon = Icons.Rounded.LocationOn,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
            }
            
            item {
                PremiumTextField(
                    value = gstNumber,
                    onValueChange = { gstNumber = it },
                    label = "Tax ID / GST",
                    placeholder = "",
                    icon = Icons.Rounded.ReceiptLong,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PremiumTextField(
                            value = openingTime,
                            onValueChange = { openingTime = it },
                            label = "Opening Time",
                            placeholder = "e.g. 10:00 AM",
                            icon = Icons.Rounded.AccessTime,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PremiumTextField(
                            value = closingTime,
                            onValueChange = { closingTime = it },
                            label = "Closing Time",
                            placeholder = "e.g. 11:00 PM",
                            icon = Icons.Rounded.AccessTime,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
            }
            
            item {
                PrimaryButton(
                    text = "Save Changes",
                    onClick = {
                        onSave(
                            RestaurantProfile(
                                id = 1,
                                name = name,
                                businessType = businessType,
                                phone = phone,
                                email = email,
                                address = address,
                                gstNumber = gstNumber,
                                openingTime = openingTime,
                                closingTime = closingTime,
                                logoUri = logoUri
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
