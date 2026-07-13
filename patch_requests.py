import os

files = [
    "app/src/main/java/com/example/ui/screens/DeletionRequestsViewModel.kt",
    "app/src/main/java/com/example/ui/screens/DeletionRequestsScreen.kt",
    "app/src/main/java/com/example/ui/screens/HomeViewModel.kt",
    "app/src/main/java/com/example/ui/screens/ReportsViewModel.kt",
    "app/src/main/java/com/example/ui/screens/TransactionHistoryScreen.kt",
    "app/src/main/java/com/example/ui/screens/HomeScreen.kt",
    "app/src/main/java/com/example/ui/screens/MainScreen.kt"
]

for file in files:
    with open(file, "r") as f:
        content = f.read()

    content = content.replace("RecycleBin", "DeletionRequests")
    content = content.replace("Recycle Bin", "Deletion Requests")
    content = content.replace("moveToRecycleBin", "requestDeletion")
    content = content.replace("getDeletedBills", "getDeletionRequests")
    content = content.replace("restoreBill", "rejectDeletion")
    content = content.replace("deletedBills", "pendingRequests")
    content = content.replace("recycleBin", "deletionRequests")
    content = content.replace("showRecycleBin", "showDeletionRequests")
    
    # Custom toast replacements
    content = content.replace("Bill moved to Recycle Bin", "Deletion requested")

    with open(file, "w") as f:
        f.write(content)

