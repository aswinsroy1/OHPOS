import re
import os
import glob

def replace_duplicate_labels(match):
    full_match = match.group(0)
    lines = full_match.split('\n')
    label_count = 0
    for i, line in enumerate(lines):
        if 'label =' in line:
            label_count += 1
            if label_count == 2:
                lines[i] = line.replace('label =', 'placeholder =')
    return '\n'.join(lines)

for filename in glob.glob("app/src/main/java/com/example/ui/screens/*.kt"):
    with open(filename, "r") as f:
        content = f.read()
    
    # Use regex to find PremiumTextField(...) blocks
    content = re.sub(r'PremiumTextField\([^)]+\)', replace_duplicate_labels, content)
    
    with open(filename, "w") as f:
        f.write(content)
