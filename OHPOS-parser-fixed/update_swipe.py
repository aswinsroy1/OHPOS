import re

with open('app/src/main/java/com/example/ui/components/SwipeActionCard.kt', 'r') as f:
    content = f.read()

# Add SwipeCardManager object at the end of the file
if 'object SwipeCardManager' not in content:
    content += '''

object SwipeCardManager {
    var currentlyOpenCardId by mutableStateOf<Any?>(null)
}
'''

# Add detectTapGestures import if missing
if 'import androidx.compose.foundation.gestures.detectTapGestures' not in content:
    content = content.replace('import androidx.compose.foundation.gestures.detectHorizontalDragGestures', 
                              'import androidx.compose.foundation.gestures.detectHorizontalDragGestures\\nimport androidx.compose.foundation.gestures.detectTapGestures')

with open('app/src/main/java/com/example/ui/components/SwipeActionCard.kt', 'w') as f:
    f.write(content)
