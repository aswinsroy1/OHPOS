with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    "            onClick = { itemRect?.let { onClick(it) } }\n        )",
    "            onClick = { itemRect?.let { onClick(it) } },\n            isPendingDeletion = billWithItems.bill.state == \"PENDING_DELETION\"\n        )"
)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
