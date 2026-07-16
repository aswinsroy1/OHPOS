import sys
import glob

for filename in glob.glob("app/src/main/java/com/example/ui/screens/*ViewModel.kt"):
    with open(filename, "r") as f:
        content = f.read()
    
    if "import kotlinx.coroutines.flow.*" not in content:
        content = content.replace("import kotlinx.coroutines.flow.first", "import kotlinx.coroutines.flow.*")
        with open(filename, "w") as f:
            f.write(content)
