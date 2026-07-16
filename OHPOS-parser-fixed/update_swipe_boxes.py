import re

with open('app/src/main/java/com/example/ui/components/SwipeActionCard.kt', 'r') as f:
    content = f.read()

replacement = '''
            // Action Backgrounds
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(vertical = 0.dp)
                    .clip(AppTheme.radius.lg)
                    .background(AppTheme.colors.accent.copy(alpha = 0.2f))
            ) {
                // Delete Background (Left to Right drag)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .bouncyClickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            coroutineScope.launch {
                                isVisible = false
                                delay(300)
                                onDelete()
                            }
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(AppTheme.dimensions.listItemIconSize)
                            .padding(start = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = AppTheme.colors.accent,
                            modifier = Modifier.size(AppTheme.dimensions.iconSizeMd)
                        )
                    }
                }
                
                // Print Background (Right to Left drag)
                if (onPrint != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight()
                            .align(Alignment.CenterEnd)
                            .bouncyClickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                coroutineScope.launch {
                                    offsetX.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
                                    onPrint()
                                }
                            },
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(AppTheme.dimensions.listItemIconSize)
                                .padding(end = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Print,
                                contentDescription = "Print",
                                tint = AppTheme.colors.accent,
                                modifier = Modifier.size(AppTheme.dimensions.iconSizeMd)
                            )
                        }
                    }
                }
            }
'''

content = re.sub(r'// Action Backgrounds.*?// Foreground Content', replacement.strip() + '\n\n            // Foreground Content', content, flags=re.DOTALL)
# also we need import fillMaxHeight if not imported.
if 'import androidx.compose.foundation.layout.fillMaxHeight' not in content:
    content = content.replace('import androidx.compose.foundation.layout.fillMaxWidth', 'import androidx.compose.foundation.layout.fillMaxHeight\nimport androidx.compose.foundation.layout.fillMaxWidth')

with open('app/src/main/java/com/example/ui/components/SwipeActionCard.kt', 'w') as f:
    f.write(content)
