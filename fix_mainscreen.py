with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

content = content.replace("@Composable\nfun MainScreen", "@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun MainScreen")

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
