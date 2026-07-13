import re
with open("app/src/main/java/com/example/ui/components/PdfImportReviewOverlay.kt", "r") as f:
    text = f.read()
text = text.replace("    }\n}\n}\n\nenum class ImportActionType", "    }\n}\n\nenum class ImportActionType")
with open("app/src/main/java/com/example/ui/components/PdfImportReviewOverlay.kt", "w") as f:
    f.write(text)
