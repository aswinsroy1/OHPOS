import re
import glob

for filename in glob.glob("app/src/main/java/com/example/ui/screens/*.kt"):
    with open(filename, "r") as f:
        content = f.read()
    
    content = re.sub(r'AppSearchBar\([\s\S]*?\)', lambda m: m.group(0).replace('label = "Search menu..."', 'placeholder = "Search menu..."').replace('label = "Search transactions..."', 'placeholder = "Search transactions..."'), content)
    
    with open(filename, "w") as f:
        f.write(content)
