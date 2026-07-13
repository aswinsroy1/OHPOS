import sys
import os
import glob

for filename in glob.glob("app/src/main/java/com/example/ui/screens/*ViewModel.kt"):
    with open(filename, "r") as f:
        content = f.read()
    
    import re
    content = re.sub(r"kotlinx\.coroutines\.flow\.first\((.*?)\)", r"\1.first()", content)
    
    with open(filename, "w") as f:
        f.write(content)
