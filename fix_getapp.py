with open("app/src/main/java/com/example/ui/screens/BillingViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("getApplication(),", "getApplication<Application>(),")
content = content.replace("getApplication()", "getApplication<Application>()")

with open("app/src/main/java/com/example/ui/screens/BillingViewModel.kt", "w") as f:
    f.write(content)
