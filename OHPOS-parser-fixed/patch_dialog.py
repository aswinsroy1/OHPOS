import re

with open("app/src/main/java/com/example/ui/components/PdfImportReviewOverlay.kt", "r") as f:
    text = f.read()

target = """@Composable
fun PdfImportReviewOverlay(
    isVisible: Boolean,
    parsedItems: List<MenuItem>,
    existingItems: List<MenuItem>,
    onDismissRequest: () -> Unit,
    onItemChange: (Int, MenuItem) -> Unit,
    onImportConfirmed: (List<ImportAction>) -> Unit
) {
    if (!isVisible) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {}
    ) {"""

replacement = """@Composable
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
        ) {"""

text = text.replace(target, replacement)
text = text.replace("    }\n}\n\nenum class", "    }\n    }\n}\n\nenum class")

with open("app/src/main/java/com/example/ui/components/PdfImportReviewOverlay.kt", "w") as f:
    f.write(text)
