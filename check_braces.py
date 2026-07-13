with open("app/src/main/java/com/example/ui/screens/SettingsScreen.kt", "r") as f:
    text = f.read()

count = 0
for i, c in enumerate(text):
    if c == '{':
        count += 1
    elif c == '}':
        count -= 1
    if count == 0 and c == '}':
        print(f"Top level brace closed at index {i}")
        break

if count > 0:
    print(f"Missing {count} closing braces")
