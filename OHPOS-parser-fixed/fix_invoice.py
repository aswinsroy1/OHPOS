import re

with open("app/src/main/java/com/example/ui/components/InvoicePreviewOverlay.kt", "r") as f:
    content = f.read()

# Replace all occurrences of the bad sed
bad_string = """val expandFraction by transition.animateFloat(
                    transitionSpec = { PremiumMotion.defaultSpring() },
                    label = "expandFraction"
                ) { visible -> if (visible) 1f else 0f }

                Box("""

content = content.replace(bad_string, "Box(")

with open("app/src/main/java/com/example/ui/components/InvoicePreviewOverlay.kt", "w") as f:
    f.write(content)
