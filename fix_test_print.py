with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content = f.read()

import re

# We can replace the onClick of Test Print
content = re.sub(
    r'SettingItemData\("Test Print", Icons\.Rounded\.DoneAll, onClick = \{.*?\}\)',
    'SettingItemData("Test Print", Icons.Rounded.DoneAll, onClick = { '
    'val printer = savedPrinters.firstOrNull { it.isDefault } ?: savedPrinters.firstOrNull(); '
    'if (printer != null) printerViewModel.testPrint(context, printer) '
    'else Toast.makeText(context, "No printer configured.", Toast.LENGTH_LONG).show() '
    '})',
    content
)

# And remove the showTestPrintSheet logic from SettingsScreen
# First remove `var showTestPrintSheet`
content = re.sub(r'var showTestPrintSheet by remember \{ mutableStateOf\(false\) \}\n\s*', '', content)

# Then remove the bottom sheet block
# We can find `if (showTestPrintSheet) {` and remove it along with its block.
start = content.find('if (showTestPrintSheet) {')
if start != -1:
    # Need to balance braces to find the end of the `if` block
    brace_count = 0
    in_block = False
    for i in range(start, len(content)):
        if content[i] == '{':
            brace_count += 1
            in_block = True
        elif content[i] == '}':
            brace_count -= 1
        
        if in_block and brace_count == 0:
            end = i + 1
            content = content[:start] + content[end:]
            break

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content)
