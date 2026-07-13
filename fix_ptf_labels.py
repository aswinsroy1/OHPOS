import os
import glob
import re

for filename in glob.glob("app/src/main/java/com/example/ui/screens/*.kt"):
    with open(filename, "r") as f:
        content = f.read()
    
    # It currently says placeholder = "..." everywhere because of my previous script
    # I want to change placeholder = "..." to label = "..."
    # EXCEPT for placeholder = "" which was there originally
    content = re.sub(r'placeholder(\s*=\s*".+?")', r'label\1', content)
    content = re.sub(r'placeholder(\s*=\s*if.*?else.*?)(\n|\r)', r'label\1\2', content)

    with open(filename, "w") as f:
        f.write(content)
