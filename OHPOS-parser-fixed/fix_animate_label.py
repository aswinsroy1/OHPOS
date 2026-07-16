import os
import glob
import re

for filename in glob.glob("app/src/main/java/com/example/ui/screens/*.kt"):
    with open(filename, "r") as f:
        content = f.read()
    
    # Fix animateFloatAsState label
    content = content.replace('placeholder = "gst"', 'label = "gst"')
    content = content.replace('placeholder = "cartList"', 'label = "cartList"')
    
    with open(filename, "w") as f:
        f.write(content)
