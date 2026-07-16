with open("app/src/main/java/com/example/util/PrinterManager.kt", "r") as f:
    content = f.read()
if "import kotlinx.coroutines.flow.first" not in content:
    content = content.replace("import kotlinx.coroutines.Dispatchers", "import kotlinx.coroutines.flow.first\nimport kotlinx.coroutines.Dispatchers")
with open("app/src/main/java/com/example/util/PrinterManager.kt", "w") as f:
    f.write(content)
