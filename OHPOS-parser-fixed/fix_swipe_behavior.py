import re

with open('app/src/main/java/com/example/ui/components/SwipeActionCard.kt', 'r') as f:
    content = f.read()

# 1. Update LaunchedEffect for currentlyOpenCardId
old_launched_effect = '''    LaunchedEffect(currentlyOpenCardId) {
        if (currentlyOpenCardId != cardId && offsetX.targetValue != 0f) {
            offsetX.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
        }
    }'''

new_launched_effect = '''    val globalOpenCardId = SwipeCardManager.currentlyOpenCardId
    
    LaunchedEffect(globalOpenCardId) {
        if (globalOpenCardId != cardId && offsetX.targetValue != 0f) {
            offsetX.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
        }
    }
    
    LaunchedEffect(offsetX.targetValue) {
        if (offsetX.targetValue != 0f) {
            delay(4000)
            offsetX.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
            if (SwipeCardManager.currentlyOpenCardId == cardId) {
                SwipeCardManager.currentlyOpenCardId = null
            }
        }
    }'''

content = content.replace(old_launched_effect, new_launched_effect)

# 2. Update onCardOpen(cardId) to also set SwipeCardManager
content = content.replace('onCardOpen(cardId)', 'onCardOpen(cardId)\\n                                        SwipeCardManager.currentlyOpenCardId = cardId')

# 3. Add Tap to Close overlay
old_foreground_content = '''            ) {
                content()
            }
        }
    }
}'''

new_foreground_content = '''            ) {
                content()
                
                if (offsetX.targetValue != 0f) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        coroutineScope.launch {
                                            offsetX.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
                                            if (SwipeCardManager.currentlyOpenCardId == cardId) {
                                                SwipeCardManager.currentlyOpenCardId = null
                                            }
                                        }
                                    }
                                )
                            }
                    )
                }
            }
        }
    }
}'''

content = content.replace(old_foreground_content, new_foreground_content)

with open('app/src/main/java/com/example/ui/components/SwipeActionCard.kt', 'w') as f:
    f.write(content)
