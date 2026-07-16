with open("app/src/main/java/com/example/ui/screens/DeletionRequestsScreen.kt", "r") as f:
    content = f.read()

content = content.replace('contentDescription = "Restore",', 'contentDescription = "Reject Request",')
content = content.replace('text = "Restore",', 'text = "Reject Request",')

with open("app/src/main/java/com/example/ui/screens/DeletionRequestsScreen.kt", "w") as f:
    f.write(content)
