import glob

for filename in glob.glob("app/src/main/java/com/example/ui/screens/*.kt"):
    with open(filename, "r") as f:
        content = f.read()
    
    if "@androidx.compose.material3.ExperimentalMaterial3Api" not in content and "ModalBottomSheet" in content:
        content = content.replace("@Composable\nfun", "@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun")
    
    if "SettingsNavGraph.kt" in filename:
        content = content.replace("@Composable\nfun SettingsNavGraph", "@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun SettingsNavGraph")

    with open(filename, "w") as f:
        f.write(content)
