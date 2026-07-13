import os

files = [
    "app/src/main/java/com/example/ui/screens/TransactionHistoryScreen.kt",
    "app/src/main/java/com/example/ui/screens/HomeScreen.kt",
]

for file in files:
    with open(file, "r") as f:
        content = f.read()

    content = content.replace('toastMessage = "Bill moved to Deletion Requests"', 'toastMessage = "Deletion requested"')

    with open(file, "w") as f:
        f.write(content)
