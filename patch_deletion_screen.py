with open("app/src/main/java/com/example/ui/screens/DeletionRequestsScreen.kt", "r") as f:
    content = f.read()

content = content.replace("Restore Bill", "Reject Request")
content = content.replace("Bill restored.", "Deletion request rejected.")

content = content.replace("Delete Permanently", "Approve Deletion")
content = content.replace("Bill permanently deleted.", "Invoice deleted.")

content = content.replace("In Deletion Requests", "Pending Deletion")

with open("app/src/main/java/com/example/ui/screens/DeletionRequestsScreen.kt", "w") as f:
    f.write(content)
