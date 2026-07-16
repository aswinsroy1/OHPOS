package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.data.BillWithItems
import com.example.ui.components.*
import com.example.ui.theme.AppTheme
import com.example.util.PinManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

enum class PinModalState {
    None,
    SetPin,
    LockOptions,
    EnterCurrentForChange,
    EnterNewForChange,
    ConfirmNewForChange,
    EnterCurrentForDisable
}

@Composable
fun DeletionRequestsScreen(
    onBackClick: () -> Unit,
    selectedTab: NavItem,
    onTabSelected: (NavItem) -> Unit,
    viewModel: DeletionRequestsViewModel = viewModel()
) {
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val context = LocalContext.current
    val pinManager = remember { PinManager(context) }
    
    // Initial entry lock state
    var isUnlocked by remember { mutableStateOf(!pinManager.hasPin()) }
    var showBiometricPrompt by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isUnlocked && pinManager.hasPin()) {
            val biometricManager = androidx.biometric.BiometricManager.from(context)
            val canAuthenticate = biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK)
            if (canAuthenticate == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
                showBiometricPrompt = true
            }
        }
    }

    LaunchedEffect(showBiometricPrompt) {
        if (showBiometricPrompt) {
            val fragmentActivity = context as? androidx.fragment.app.FragmentActivity
            if (fragmentActivity != null) {
                val executor = androidx.core.content.ContextCompat.getMainExecutor(context)
                val biometricPrompt = androidx.biometric.BiometricPrompt(fragmentActivity, executor,
                    object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            // Fallback to PIN
                            showBiometricPrompt = false
                        }

                        override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            isUnlocked = true
                            showBiometricPrompt = false
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            // Framework handles UI for failed attempt, wait for success or error
                        }
                    })

                val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authentication Required")
                    .setSubtitle("Authenticate to view deletion requests")
                    .setNegativeButtonText("Use PIN")
                    .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK)
                    .build()

                biometricPrompt.authenticate(promptInfo)
            } else {
                showBiometricPrompt = false
            }
        }
    }

    var enterPinInput by remember { mutableStateOf("") }
    var enterPinError by remember { mutableStateOf("") }

    // Modal overlay states
    var activePinModal by remember { mutableStateOf(PinModalState.None) }
    
    // Set PIN states
    var setPinInput by remember { mutableStateOf("") }
    var setPinConfirmInput by remember { mutableStateOf("") }
    var setPinFocusedField by remember { mutableStateOf(0) } // 0 for PIN, 1 for Confirm PIN
    var setPinError by remember { mutableStateOf("") }
    
    // Change PIN states
    var changeCurrentInput by remember { mutableStateOf("") }
    var changeCurrentError by remember { mutableStateOf("") }
    var changeNewInput by remember { mutableStateOf("") }
    var changeNewError by remember { mutableStateOf("") }
    var changeConfirmInput by remember { mutableStateOf("") }
    var changeConfirmError by remember { mutableStateOf("") }
    var storedNewPin by rememberSaveable { mutableStateOf("") }
    
    // Disable PIN states
    var disableCurrentInput by remember { mutableStateOf("") }
    var disableCurrentError by remember { mutableStateOf("") }

    // General UI states
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var selectedBillForContext by remember { mutableStateOf<BillWithItems?>(null) }
    var showContextPopup by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Intercept system Back button to always return to Home
    BackHandler {
        onBackClick()
    }

    LaunchedEffect(showToast) {
        if (showToast) {
            delay(3000)
            showToast = false
        }
    }

    // Reset modals' input values when modal state changes
    LaunchedEffect(activePinModal) {
        setPinInput = ""
        setPinConfirmInput = ""
        setPinFocusedField = 0
        setPinError = ""
        
        changeCurrentInput = ""
        changeCurrentError = ""
        if (activePinModal != PinModalState.ConfirmNewForChange) {
            changeNewInput = ""
        }
        changeNewError = ""
        changeConfirmInput = ""
        changeConfirmError = ""
        if (activePinModal == PinModalState.None) {
            storedNewPin = ""
        }
        
        disableCurrentInput = ""
        disableCurrentError = ""
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isUnlocked) {
            // PIN ENTRY LOCK SCREEN (Opens BEFORE showing requests)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AppTheme.spacing.lg)
                        .windowInsetsPadding(WindowInsets.statusBars),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = AppTheme.colors.accent,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                    Text(
                        text = "Enter Security PIN",
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter 4-digit PIN to access Deletion Requests",
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    
                    // Masked PIN dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (i in 0 until 4) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i < enterPinInput.length) AppTheme.colors.accent
                                        else AppTheme.colors.surfaceLighter
                                    )
                                    .border(1.5.dp, AppTheme.colors.borderLight, CircleShape)
                            )
                        }
                    }
                    
                    if (enterPinError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                        Text(
                            text = enterPinError,
                            style = AppTheme.typography.bodyMedium,
                            color = Color(0xFFE57373),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    
                    // Numeric keypad
                    NumericKeypad(
                        onDigitClick = { digit ->
                            if (enterPinInput.length < 4) {
                                enterPinInput += digit
                            }
                        },
                        onDeleteClick = {
                            if (enterPinInput.isNotEmpty()) {
                                enterPinInput = enterPinInput.dropLast(1)
                            }
                        },
                        modifier = Modifier.width(320.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                    
                    // Buttons: Cancel, Unlock
                    Row(
                        modifier = Modifier.width(320.dp),
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PrimaryButton(
                                text = "Cancel",
                                onClick = onBackClick,
                                containerColor = AppTheme.colors.surfaceLighter,
                                contentColor = AppTheme.colors.textPrimary
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            PrimaryButton(
                                text = "Unlock",
                                onClick = {
                                    if (enterPinInput.length != 4) {
                                        enterPinError = "PIN must be exactly 4 digits"
                                    } else if (!pinManager.verifyPin(enterPinInput)) {
                                        enterPinError = "Incorrect PIN"
                                    } else {
                                        isUnlocked = true
                                    }
                                },
                                containerColor = AppTheme.colors.accent,
                                 
                            )
                        }
                    }
                }
            }
        } else {
            // UNLOCKED DELETION REQUESTS SCREEN CONTENT
            PremiumModalOverlay(
                isVisible = showContextPopup || showDeleteConfirmDialog || activePinModal != PinModalState.None,
                onDismissRequest = { 
                    showContextPopup = false 
                    showDeleteConfirmDialog = false
                    activePinModal = PinModalState.None
                },
                content = {
                    AppScaffold(
                        topBar = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AppTheme.colors.background)
                            ) {
                                AppTopBar(
                                    title = "Deletion Requests",
                                    onBackClick = onBackClick,
                                    trailingIcon = Icons.Rounded.Lock,
                                    trailingContentDescription = "Security PIN",
                                    onTrailingClick = {
                                        if (pinManager.hasPin()) {
                                            activePinModal = PinModalState.LockOptions
                                        } else {
                                            activePinModal = PinModalState.SetPin
                                        }
                                    },
                                    modifier = Modifier
                                        .windowInsetsPadding(WindowInsets.statusBars)
                                        .padding(horizontal = AppTheme.spacing.lg)
                                )
                            }
                        }
                        // Note: bottomBar is removed completely (defaults to {})
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AppTheme.colors.background)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Spacer(modifier = Modifier.height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 68.dp))
                                
                                if (pendingRequests.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "Deletion Requests is empty",
                                            style = AppTheme.typography.bodyLarge,
                                            color = AppTheme.colors.textSecondary
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = AppTheme.spacing.lg),
                                        contentPadding = PaddingValues(
                                            top = AppTheme.spacing.md,
                                            bottom = 120.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                                        ),
                                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                                    ) {
                                        items(pendingRequests, key = { it.bill.id }) { billWithItems ->
                                            Box(
                                                modifier = Modifier.alpha(0.6f).animateItem()
                                            ) {
                                                RecycledOrderItem(
                                                    billWithItems = billWithItems,
                                                    onLongPress = {
                                                        selectedBillForContext = billWithItems
                                                        showContextPopup = true
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                overlayContent = {
                    // Item Long Press options popup
                    AnimatedVisibility(
                        visible = showContextPopup && !showDeleteConfirmDialog,
                        enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = PremiumMotion.defaultSpring()
                        ),
                        exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + slideOutVertically(
                            targetOffsetY = { it / 4 },
                            animationSpec = PremiumMotion.defaultSpring()
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .width(240.dp)
                                    .clip(AppTheme.radius.lg)
                                    .background(AppTheme.colors.surface)
                                    .border(1.dp, AppTheme.colors.borderLight, AppTheme.radius.lg)
                                    .padding(AppTheme.spacing.sm)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(AppTheme.radius.md)
                                            .bouncyClickable {
                                                selectedBillForContext?.let {
                                                    viewModel.rejectDeletion(it.bill.id)
                                                    toastMessage = "Deletion request rejected."
                                                    showToast = true
                                                }
                                                showContextPopup = false
                                            }
                                            .padding(AppTheme.spacing.md),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Restore,
                                            contentDescription = "Reject Request",
                                            tint = AppTheme.colors.textPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                                        Text(
                                            text = "Reject Request",
                                            style = AppTheme.typography.bodyLarge,
                                            color = AppTheme.colors.textPrimary
                                        )
                                    }
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(AppTheme.radius.md)
                                            .bouncyClickable {
                                                showContextPopup = false
                                                showDeleteConfirmDialog = true
                                            }
                                            .padding(AppTheme.spacing.md),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteForever,
                                            contentDescription = "Approve Deletion",
                                            tint = AppTheme.colors.accent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                                        Text(
                                            text = "Approve Deletion",
                                            style = AppTheme.typography.bodyLarge,
                                            color = AppTheme.colors.accent
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Delete permanently confirm dialog
                    AnimatedVisibility(
                        visible = showDeleteConfirmDialog,
                        enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = PremiumMotion.defaultSpring()
                        ),
                        exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + slideOutVertically(
                            targetOffsetY = { it / 4 },
                            animationSpec = PremiumMotion.defaultSpring()
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .width(300.dp)
                                    .clip(AppTheme.radius.lg)
                                    .background(AppTheme.colors.surface)
                                    .border(1.dp, AppTheme.colors.borderLight, AppTheme.radius.lg)
                                    .padding(AppTheme.spacing.lg)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.WarningAmber,
                                        contentDescription = "Warning",
                                        tint = AppTheme.colors.accent,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                                    Text(
                                        text = "Delete permanently?",
                                        style = AppTheme.typography.titleMedium,
                                        color = AppTheme.colors.textPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                                    Text(
                                        text = "This bill will be permanently deleted and cannot be recovered.",
                                        style = AppTheme.typography.bodyMedium,
                                        color = AppTheme.colors.textSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            PrimaryButton(
                                                text = "Cancel",
                                                onClick = { showDeleteConfirmDialog = false },
                                                containerColor = AppTheme.colors.surfaceLighter,
                                                contentColor = AppTheme.colors.textPrimary
                                            )
                                        }
                                        Box(modifier = Modifier.weight(1f)) {
                                            PrimaryButton(
                                                text = "Delete",
                                                onClick = {
                                                    selectedBillForContext?.let {
                                                        viewModel.deleteBillPermanently(it.bill.id)
                                                        toastMessage = "Invoice deleted."
                                                        showToast = true
                                                    }
                                                    showDeleteConfirmDialog = false
                                                },
                                                containerColor = AppTheme.colors.accent,
                                                 
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Security PIN modal systems
                    AnimatedVisibility(
                        visible = activePinModal != PinModalState.None,
                        enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = PremiumMotion.defaultSpring()
                        ),
                        exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + slideOutVertically(
                            targetOffsetY = { it / 4 },
                            animationSpec = PremiumMotion.defaultSpring()
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .width(if (activePinModal == PinModalState.LockOptions) 240.dp else 340.dp)
                                    .clip(AppTheme.radius.lg)
                                    .background(AppTheme.colors.surface)
                                    .border(1.dp, AppTheme.colors.borderLight, AppTheme.radius.lg)
                            ) {
                                when (activePinModal) {
                                    PinModalState.SetPin -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(AppTheme.spacing.lg),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Set Security PIN",
                                                style = AppTheme.typography.titleLarge,
                                                color = AppTheme.colors.textPrimary
                                            )
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                                            
                                            // Label and Dots for PIN field
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(AppTheme.radius.md)
                                                    .background(if (setPinFocusedField == 0) AppTheme.colors.surfaceLighter else Color.Transparent)
                                                    .border(
                                                        1.dp,
                                                        if (setPinFocusedField == 0) AppTheme.colors.accent else AppTheme.colors.borderLight,
                                                        AppTheme.radius.md
                                                    )
                                                    .bouncyClickable { setPinFocusedField = 0 }
                                                    .padding(AppTheme.spacing.md),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "PIN",
                                                    style = AppTheme.typography.labelMedium,
                                                    color = if (setPinFocusedField == 0) AppTheme.colors.accent else AppTheme.colors.textSecondary
                                                )
                                                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    for (i in 0 until 4) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(12.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    if (i < setPinInput.length) AppTheme.colors.accent
                                                                    else AppTheme.colors.surface
                                                                )
                                                                .border(1.dp, AppTheme.colors.borderLight, CircleShape)
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                                            
                                            // Label and Dots for Confirm PIN field
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(AppTheme.radius.md)
                                                    .background(if (setPinFocusedField == 1) AppTheme.colors.surfaceLighter else Color.Transparent)
                                                    .border(
                                                        1.dp,
                                                        if (setPinFocusedField == 1) AppTheme.colors.accent else AppTheme.colors.borderLight,
                                                        AppTheme.radius.md
                                                    )
                                                    .bouncyClickable { setPinFocusedField = 1 }
                                                    .padding(AppTheme.spacing.md),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "Confirm PIN",
                                                    style = AppTheme.typography.labelMedium,
                                                    color = if (setPinFocusedField == 1) AppTheme.colors.accent else AppTheme.colors.textSecondary
                                                )
                                                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    for (i in 0 until 4) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(12.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    if (i < setPinConfirmInput.length) AppTheme.colors.accent
                                                                    else AppTheme.colors.surface
                                                                )
                                                                .border(1.dp, AppTheme.colors.borderLight, CircleShape)
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            if (setPinError.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                                                Text(
                                                    text = setPinError,
                                                    style = AppTheme.typography.bodyMedium,
                                                    color = Color(0xFFE57373),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                                            
                                            NumericKeypad(
                                                onDigitClick = { digit ->
                                                    if (setPinFocusedField == 0) {
                                                        if (setPinInput.length < 4) {
                                                            setPinInput += digit
                                                        }
                                                    } else {
                                                        if (setPinConfirmInput.length < 4) {
                                                            setPinConfirmInput += digit
                                                        }
                                                    }
                                                },
                                                onDeleteClick = {
                                                    if (setPinFocusedField == 0) {
                                                        if (setPinInput.isNotEmpty()) {
                                                            setPinInput = setPinInput.dropLast(1)
                                                        }
                                                    } else {
                                                        if (setPinConfirmInput.isNotEmpty()) {
                                                            setPinConfirmInput = setPinConfirmInput.dropLast(1)
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                                            ) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    PrimaryButton(
                                                        text = "Cancel",
                                                        onClick = { activePinModal = PinModalState.None },
                                                        containerColor = AppTheme.colors.surfaceLighter,
                                                        contentColor = AppTheme.colors.textPrimary
                                                    )
                                                }
                                                Box(modifier = Modifier.weight(1f)) {
                                                    PrimaryButton(
                                                        text = "Save PIN",
                                                        onClick = {
                                                            if (setPinInput.length != 4 || setPinConfirmInput.length != 4) {
                                                                setPinError = "Both fields are required (exactly 4 digits)"
                                                            } else if (setPinInput != setPinConfirmInput) {
                                                                setPinError = "PINs do not match"
                                                            } else {
                                                                pinManager.setPin(setPinInput)
                                                                toastMessage = "Security PIN created"
                                                                showToast = true
                                                                activePinModal = PinModalState.None
                                                            }
                                                        },
                                                        containerColor = AppTheme.colors.accent,
                                                         
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    PinModalState.LockOptions -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(AppTheme.spacing.md)
                                        ) {
                                            Text(
                                                text = "Security PIN",
                                                style = AppTheme.typography.titleMedium,
                                                color = AppTheme.colors.textPrimary,
                                                modifier = Modifier.padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm)
                                            )
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                                            
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(AppTheme.radius.md)
                                                    .bouncyClickable {
                                                        activePinModal = PinModalState.EnterCurrentForChange
                                                    }
                                                    .padding(AppTheme.spacing.md),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Edit,
                                                    contentDescription = "Change PIN",
                                                    tint = AppTheme.colors.textPrimary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                                                Text(
                                                    text = "Change PIN",
                                                    style = AppTheme.typography.bodyLarge,
                                                    color = AppTheme.colors.textPrimary
                                                )
                                            }
                                            
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(AppTheme.radius.md)
                                                    .bouncyClickable {
                                                        activePinModal = PinModalState.EnterCurrentForDisable
                                                    }
                                                    .padding(AppTheme.spacing.md),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.LockOpen,
                                                    contentDescription = "Disable PIN",
                                                    tint = AppTheme.colors.textPrimary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                                                Text(
                                                    text = "Disable PIN",
                                                    style = AppTheme.typography.bodyLarge,
                                                    color = AppTheme.colors.textPrimary
                                                )
                                            }
                                        }
                                    }
                                    PinModalState.EnterCurrentForChange -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(AppTheme.spacing.lg),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Enter Current PIN",
                                                style = AppTheme.typography.titleMedium,
                                                color = AppTheme.colors.textPrimary
                                            )
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                                            
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                for (i in 0 until 4) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(16.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (i < changeCurrentInput.length) AppTheme.colors.accent
                                                                else AppTheme.colors.surfaceLighter
                                                            )
                                                            .border(1.dp, AppTheme.colors.borderLight, CircleShape)
                                                    )
                                                }
                                            }
                                            
                                            if (changeCurrentError.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                                                Text(
                                                    text = changeCurrentError,
                                                    style = AppTheme.typography.bodyMedium,
                                                    color = Color(0xFFE57373),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                                            
                                            NumericKeypad(
                                                onDigitClick = { digit ->
                                                    if (changeCurrentInput.length < 4) {
                                                        changeCurrentInput += digit
                                                    }
                                                },
                                                onDeleteClick = {
                                                    if (changeCurrentInput.isNotEmpty()) {
                                                        changeCurrentInput = changeCurrentInput.dropLast(1)
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                                            ) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    PrimaryButton(
                                                        text = "Cancel",
                                                        onClick = { activePinModal = PinModalState.None },
                                                        containerColor = AppTheme.colors.surfaceLighter,
                                                        contentColor = AppTheme.colors.textPrimary
                                                    )
                                                }
                                                Box(modifier = Modifier.weight(1f)) {
                                                    PrimaryButton(
                                                        text = "Next",
                                                        onClick = {
                                                            if (changeCurrentInput.length != 4) {
                                                                changeCurrentError = "PIN must be 4 digits"
                                                            } else if (!pinManager.verifyPin(changeCurrentInput)) {
                                                                changeCurrentError = "Incorrect PIN"
                                                            } else {
                                                                activePinModal = PinModalState.EnterNewForChange
                                                            }
                                                        },
                                                        containerColor = AppTheme.colors.accent,
                                                         
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    PinModalState.EnterNewForChange -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(AppTheme.spacing.lg),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Enter New PIN",
                                                style = AppTheme.typography.titleMedium,
                                                color = AppTheme.colors.textPrimary
                                            )
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                                            
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                for (i in 0 until 4) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(16.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (i < changeNewInput.length) AppTheme.colors.accent
                                                                else AppTheme.colors.surfaceLighter
                                                            )
                                                            .border(1.dp, AppTheme.colors.borderLight, CircleShape)
                                                    )
                                                }
                                            }
                                            
                                            if (changeNewError.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                                                Text(
                                                    text = changeNewError,
                                                    style = AppTheme.typography.bodyMedium,
                                                    color = Color(0xFFE57373),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                                            
                                            NumericKeypad(
                                                onDigitClick = { digit ->
                                                    if (changeNewInput.length < 4) {
                                                        changeNewInput += digit
                                                    }
                                                },
                                                onDeleteClick = {
                                                    if (changeNewInput.isNotEmpty()) {
                                                        changeNewInput = changeNewInput.dropLast(1)
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                                            ) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    PrimaryButton(
                                                        text = "Cancel",
                                                        onClick = { activePinModal = PinModalState.None },
                                                        containerColor = AppTheme.colors.surfaceLighter,
                                                        contentColor = AppTheme.colors.textPrimary
                                                    )
                                                }
                                                Box(modifier = Modifier.weight(1f)) {
                                                    PrimaryButton(
                                                        text = "Next",
                                                        onClick = {
                                                            if (changeNewInput.length != 4) {
                                                                changeNewError = "PIN must be exactly 4 digits"
                                                            } else {
                                                                storedNewPin = changeNewInput
                                                                activePinModal = PinModalState.ConfirmNewForChange
                                                            }
                                                        },
                                                        containerColor = AppTheme.colors.accent,
                                                         
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    PinModalState.ConfirmNewForChange -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(AppTheme.spacing.lg),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Confirm New PIN",
                                                style = AppTheme.typography.titleMedium,
                                                color = AppTheme.colors.textPrimary
                                            )
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                                            
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                for (i in 0 until 4) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(16.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (i < changeConfirmInput.length) AppTheme.colors.accent
                                                                else AppTheme.colors.surfaceLighter
                                                            )
                                                            .border(1.dp, AppTheme.colors.borderLight, CircleShape)
                                                    )
                                                }
                                            }
                                            
                                            if (changeConfirmError.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                                                Text(
                                                    text = changeConfirmError,
                                                    style = AppTheme.typography.bodyMedium,
                                                    color = Color(0xFFE57373),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                                            
                                            NumericKeypad(
                                                onDigitClick = { digit ->
                                                    if (changeConfirmInput.length < 4) {
                                                        changeConfirmInput += digit
                                                    }
                                                },
                                                onDeleteClick = {
                                                    if (changeConfirmInput.isNotEmpty()) {
                                                        changeConfirmInput = changeConfirmInput.dropLast(1)
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                                            ) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    PrimaryButton(
                                                        text = "Cancel",
                                                        onClick = { activePinModal = PinModalState.None },
                                                        containerColor = AppTheme.colors.surfaceLighter,
                                                        contentColor = AppTheme.colors.textPrimary
                                                    )
                                                }
                                                Box(modifier = Modifier.weight(1f)) {
                                                    PrimaryButton(
                                                        text = "Save",
                                                        onClick = {
                                                            if (changeConfirmInput.length != 4) {
                                                                changeConfirmError = "PIN must be exactly 4 digits"
                                                            } else if (changeConfirmInput != storedNewPin) {
                                                                changeConfirmError = "PINs do not match"
                                                                changeConfirmInput = ""
                                                            } else {
                                                                pinManager.setPin(storedNewPin)
                                                                storedNewPin = ""
                                                                changeNewInput = ""
                                                                changeConfirmInput = ""
                                                                changeCurrentInput = ""
                                                                toastMessage = "PIN updated successfully"
                                                                showToast = true
                                                                activePinModal = PinModalState.None
                                                            }
                                                        },
                                                        containerColor = AppTheme.colors.accent,
                                                         
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    PinModalState.EnterCurrentForDisable -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(AppTheme.spacing.lg),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Disable PIN",
                                                style = AppTheme.typography.titleMedium,
                                                color = AppTheme.colors.textPrimary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Enter current PIN to confirm",
                                                style = AppTheme.typography.bodyMedium,
                                                color = AppTheme.colors.textSecondary,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                                            
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                for (i in 0 until 4) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(16.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                if (i < disableCurrentInput.length) AppTheme.colors.accent
                                                                else AppTheme.colors.surfaceLighter
                                                            )
                                                            .border(1.dp, AppTheme.colors.borderLight, CircleShape)
                                                    )
                                                }
                                            }
                                            
                                            if (disableCurrentError.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                                                Text(
                                                    text = disableCurrentError,
                                                    style = AppTheme.typography.bodyMedium,
                                                    color = Color(0xFFE57373),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                                            
                                            NumericKeypad(
                                                onDigitClick = { digit ->
                                                    if (disableCurrentInput.length < 4) {
                                                        disableCurrentInput += digit
                                                    }
                                                },
                                                onDeleteClick = {
                                                    if (disableCurrentInput.isNotEmpty()) {
                                                        disableCurrentInput = disableCurrentInput.dropLast(1)
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            
                                            Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                                            ) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    PrimaryButton(
                                                        text = "Cancel",
                                                        onClick = { activePinModal = PinModalState.None },
                                                        containerColor = AppTheme.colors.surfaceLighter,
                                                        contentColor = AppTheme.colors.textPrimary
                                                    )
                                                }
                                                Box(modifier = Modifier.weight(1f)) {
                                                    PrimaryButton(
                                                        text = "Disable",
                                                        onClick = {
                                                            if (disableCurrentInput.length != 4) {
                                                                disableCurrentError = "PIN must be exactly 4 digits"
                                                            } else if (!pinManager.verifyPin(disableCurrentInput)) {
                                                                disableCurrentError = "Incorrect PIN"
                                                            } else {
                                                                pinManager.clearPin()
                                                                toastMessage = "Security PIN disabled"
                                                                showToast = true
                                                                activePinModal = PinModalState.None
                                                            }
                                                        },
                                                        containerColor = AppTheme.colors.accent,
                                                         
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            )
        }
        
        // Toast Notification Overlay
        AnimatedVisibility(
            visible = showToast,
            enter = fadeIn(animationSpec = PremiumMotion.defaultSpring()) + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut(animationSpec = PremiumMotion.defaultSpring()) + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(AppTheme.radius.full)
                    .background(AppTheme.colors.surface)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), AppTheme.radius.full)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(toastMessage, style = AppTheme.typography.labelMedium, color = AppTheme.colors.textPrimary)
            }
        }
    }
}

@Composable
fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "Del")
    )
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.8f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (key.isNotEmpty()) {
                            val isAction = key == "Del"
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(AppTheme.radius.md)
                                    .background(if (isAction) AppTheme.colors.surfaceLighter else AppTheme.colors.surface)
                                    .border(1.dp, AppTheme.colors.borderLight.copy(alpha = 0.5f), AppTheme.radius.md)
                                    .bouncyClickable {
                                        if (key == "Del") {
                                            onDeleteClick()
                                        } else {
                                            onDigitClick(key)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    style = if (isAction) AppTheme.typography.bodyLarge else AppTheme.typography.titleLarge,
                                    color = AppTheme.colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecycledOrderItem(
    billWithItems: BillWithItems,
    onLongPress: () -> Unit
) {
    val date = Date(billWithItems.bill.timestamp)
    val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    
    AppListItem(
        title = "Order #${billWithItems.bill.id}",
        subtitle = formatter.format(date),
        trailingText = com.example.util.CurrencyFormatter.format(billWithItems.bill.totalAmount),
        icon = Icons.Rounded.ShoppingCart,
        statusText = "Pending Deletion",
        statusColor = AppTheme.colors.textSecondary,
        onClick = {},
        onLongPress = onLongPress
    )
}
