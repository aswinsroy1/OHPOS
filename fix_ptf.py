import re

with open("app/src/main/java/com/example/ui/components/PremiumTextField.kt", "r") as f:
    content = f.read()

content = content.replace("placeholder: String,", 'placeholder: String = "",')
content = content.replace("label: String,", 'label: String = "",')

with open("app/src/main/java/com/example/ui/components/PremiumTextField.kt", "w") as f:
    f.write(content)

# And fix SettingsScreen.kt to use onDismissRequest
with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    content2 = f.read()

content2 = content2.replace("onDismiss = ", "onDismissRequest = ")

with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "w") as f:
    f.write(content2)

# Fix PrinterSettingsScreen.kt onDismissRequest
with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "r") as f:
    content3 = f.read()

content3 = content3.replace("onDismiss = ", "onDismissRequest = ")

with open("app/src/main/java/com/example/ui/screens/PrinterSettingsScreen.kt", "w") as f:
    f.write(content3)
