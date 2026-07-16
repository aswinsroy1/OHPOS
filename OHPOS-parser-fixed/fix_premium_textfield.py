import os
import glob
import re

for filename in glob.glob("app/src/main/java/com/example/ui/screens/*.kt"):
    with open(filename, "r") as f:
        content = f.read()
    
    # Replace label = "..." with placeholder = "..." in PremiumTextField calls
    content = re.sub(r'label(\s*=\s*".*?")', r'placeholder\1', content)
    content = re.sub(r'label(\s*=\s*if.*?else.*?)(\n|\r)', r'placeholder\1\2', content)
    
    with open(filename, "w") as f:
        f.write(content)
