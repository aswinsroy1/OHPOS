import re

# Fix PdfImportReviewOverlay.kt
with open("app/src/main/java/com/example/ui/components/PdfImportReviewOverlay.kt", "r") as f:
    pdf_content = f.read()

pdf_content = pdf_content.replace("""    PremiumModalOverlay(
        isVisible = isVisible,
        onDismissRequest = onDismissRequest
    ) {""", "")

pdf_content = pdf_content.replace("""        }
    }
}""", """        }
}""")

with open("app/src/main/java/com/example/ui/components/PdfImportReviewOverlay.kt", "w") as f:
    f.write(pdf_content)

# Fix MenuScreen.kt
with open("app/src/main/java/com/example/ui/screens/MenuScreen.kt", "r") as f:
    menu = f.read()

# Update PremiumModalOverlay condition
menu = menu.replace("isVisible = selectedItemForAction != null || showAddItemForm,", "isVisible = selectedItemForAction != null || showAddItemForm || showFabMenu || pdfParsedItems.isNotEmpty(),")

# Find the newly appended overlays at the end of the file and move them inside overlayContent
# First, remove them from the end
overlays_start_idx = menu.find("    // PDF Loading Overlay")
if overlays_start_idx != -1:
    overlays_text = menu[overlays_start_idx : menu.rfind("}")-1]
    menu = menu[:overlays_start_idx] + "\n}\n"
    
    # We need to strip the extra PremiumModalOverlay around fab menu
    overlays_text = overlays_text.replace("""    PremiumModalOverlay(
        isVisible = showFabMenu,
        onDismissRequest = { showFabMenu = false }
    ) {""", """    AnimatedVisibility(
        visible = showFabMenu,
        enter = fadeIn(),
        exit = fadeOut()
    ) {""")
    
    # Add them inside overlayContent
    # Find the end of overlayContent
    # overlayContent = { ... }
    # Let's just insert before `    if (showUnsavedDialog) {`
    insert_idx = menu.find("    if (showUnsavedDialog) {")
    menu = menu[:insert_idx] + overlays_text + "\n" + menu[insert_idx:]

# Also fix the `androidx.compose.ui.platform.LocalView` call which was missing import or context in a composable scope
# The error was: `@Composable invocations can only happen from the context of a @Composable function`
# `LocalView.current` is composable, so it can't be called inside onClick block directly.
# We need to save the view earlier.
menu = menu.replace("val context = androidx.compose.ui.platform.LocalContext.current", "val context = androidx.compose.ui.platform.LocalContext.current\n    val view = androidx.compose.ui.platform.LocalView.current")
menu = menu.replace("androidx.compose.ui.platform.LocalView.current.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)", "view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)")

with open("app/src/main/java/com/example/ui/screens/MenuScreen.kt", "w") as f:
    f.write(menu)

