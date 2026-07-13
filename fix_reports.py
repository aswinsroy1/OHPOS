import re

with open("app/src/main/java/com/example/ui/screens/ReportsViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("placeholder =", "label =")

with open("app/src/main/java/com/example/ui/screens/ReportsViewModel.kt", "w") as f:
    f.write(content)
