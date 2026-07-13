import glob

for filename in glob.glob("app/src/main/java/com/example/ui/screens/*.kt"):
    with open(filename, "r") as f:
        content = f.read()
    
    content = content.replace('label = "Search', 'placeholder = "Search')
    
    with open(filename, "w") as f:
        f.write(content)
