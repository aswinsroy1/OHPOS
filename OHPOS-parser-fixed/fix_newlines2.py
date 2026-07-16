with open('app/src/main/java/com/example/ui/components/SwipeActionCard.kt', 'r') as f:
    text = f.read()

# Replace any literal '\n' string with an actual newline if it's there
text = text.replace('\\n', '\n')

with open('app/src/main/java/com/example/ui/components/SwipeActionCard.kt', 'w') as f:
    f.write(text)
