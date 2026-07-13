with open('app/src/main/java/com/example/ui/components/SwipeActionCard.kt', 'r') as f:
    content = f.read()

content = content.replace('onCardOpen(cardId)\\n', 'onCardOpen(cardId)\n')

with open('app/src/main/java/com/example/ui/components/SwipeActionCard.kt', 'w') as f:
    f.write(content)
