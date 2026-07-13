with open('app/src/main/java/com/example/ui/components/SwipeActionCard.kt', 'r') as f:
    text = f.read()

# remove all carriage returns just in case
text = text.replace('\r', '')

# Replace the first 15 lines with hardcoded to be absolutely sure
import_block = '''package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset'''

lines = text.split('\n')
# find where 'import androidx.compose.foundation.layout.offset' is
idx = 0
for i, l in enumerate(lines):
    if 'import androidx.compose.foundation.layout.offset' in l:
        idx = i
        break

new_text = import_block + '\n' + '\n'.join(lines[idx+1:])
with open('app/src/main/java/com/example/ui/components/SwipeActionCard.kt', 'w') as f:
    f.write(new_text)
