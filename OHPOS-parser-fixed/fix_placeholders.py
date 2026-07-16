import os
import glob
import re

for filename in glob.glob("app/src/main/java/com/example/ui/screens/*.kt"):
    with open(filename, "r") as f:
        content = f.read()
    
    # Replace the duplicate placeholder
    content = re.sub(r'placeholder(\s*=\s*".*?"),\s*placeholder(\s*=\s*"",)', r'label\1,\n                    placeholder\2', content)
    # also handle if they were single line or something
    content = re.sub(r'placeholder(\s*=\s*".*?")', r'label\1', content)
    
    with open(filename, "w") as f:
        f.write(content)
