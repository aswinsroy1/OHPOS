import re

with open("app/src/main/java/com/example/ui/screens/ReceiptLayoutScreen.kt", "r") as f:
    content = f.read()

# 1. Add necessary imports
imports = """import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.vector.ImageVector
"""

# Find a good place to insert imports
if "import androidx.compose.ui.focus.onFocusChanged" not in content:
    content = content.replace("import androidx.compose.runtime.*", imports + "import androidx.compose.runtime.*")

# 2. Add StatefulTextField composable at the end
stateful_field = """
@Composable
private fun StatefulTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var localValue by remember { mutableStateOf(value) }
    var isFocused by remember { mutableStateOf(false) }

    val currentLocalValue by rememberUpdatedState(localValue)
    val currentIsFocused by rememberUpdatedState(isFocused)
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    LaunchedEffect(value) {
        if (!isFocused) {
            localValue = value
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (currentIsFocused && currentLocalValue != value) {
                currentOnValueChange(currentLocalValue)
            }
        }
    }

    PremiumTextField(
        value = localValue,
        onValueChange = { localValue = it },
        label = label,
        icon = icon,
        keyboardOptions = keyboardOptions,
        modifier = Modifier.onFocusChanged { state ->
            if (isFocused && !state.isFocused && localValue != value) {
                onValueChange(localValue)
            }
            isFocused = state.isFocused
        }
    )
}
"""
if "private fun StatefulTextField(" not in content:
    content += stateful_field

# 3. Replace all PremiumTextField with StatefulTextField in the screen, except we only replace the specific ones.
# Because the fields are standard, we can just replace 'PremiumTextField(' with 'StatefulTextField('
# Wait, some have 'keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)'
# Let's just do a string replace for PremiumTextField inside the item {} blocks.
# We'll use regex for exact matches to be safe.

# Replaces PremiumTextField that are in the LazyColumn.
content = re.sub(r'PremiumTextField\((\s*value = resName.*?)\)', r'StatefulTextField(\1)', content, flags=re.DOTALL)
content = re.sub(r'PremiumTextField\((\s*value = resAddress.*?)\)', r'StatefulTextField(\1)', content, flags=re.DOTALL)
content = re.sub(r'PremiumTextField\((\s*value = resPhone.*?)\)', r'StatefulTextField(\1)', content, flags=re.DOTALL)
content = re.sub(r'PremiumTextField\((\s*value = resGst.*?)\)', r'StatefulTextField(\1)', content, flags=re.DOTALL)
content = re.sub(r'PremiumTextField\((\s*value = invoiceFooter.*?)\)', r'StatefulTextField(\1)', content, flags=re.DOTALL)
content = re.sub(r'PremiumTextField\((\s*value = thankYouMsg.*?)\)', r'StatefulTextField(\1)', content, flags=re.DOTALL)


with open("app/src/main/java/com/example/ui/screens/ReceiptLayoutScreen.kt", "w") as f:
    f.write(content)
