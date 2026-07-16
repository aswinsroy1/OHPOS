import re
with open("app/src/main/java/com/example/util/PrinterManager.kt", "r") as f:
    content = f.read()

content = content.replace("kotlinx.coroutines.flow.first(prefRepo.paperSizeFlow)", "prefRepo.paperSizeFlow.first()")

with open("app/src/main/java/com/example/util/PrinterManager.kt", "w") as f:
    f.write(content)
