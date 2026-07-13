import re
with open("app/src/main/java/com/example/util/PrinterManager.kt", "r") as f:
    content = f.read()

content = content.replace("${${paperSize} mm", "${paperSize} mm")

# And ensure import kotlinx.coroutines.flow.first is there
if "import kotlinx.coroutines.flow.first" not in content:
    content = content.replace("import kotlinx.coroutines.Dispatchers", "import kotlinx.coroutines.flow.first\nimport kotlinx.coroutines.Dispatchers")

with open("app/src/main/java/com/example/util/PrinterManager.kt", "w") as f:
    f.write(content)
