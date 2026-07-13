with open('app/src/main/java/com/example/ui/components/SwipeActionCard.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines[:15]):
    if 'detectHorizontalDragGestures' in line or 'detectTapGestures' in line:
        print(f"Line {i}: {repr(line)}")
