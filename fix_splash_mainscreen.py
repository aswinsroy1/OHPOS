import re

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

imports = """
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import kotlinx.coroutines.launch
"""

# add imports after package
content = content.replace("package com.example.ui.screens", "package com.example.ui.screens\n" + imports)

# Replace MainScreen() with new implementation
match = re.search(r'@androidx\.compose\.material3\.ExperimentalMaterial3Api\s*@Composable\s*fun MainScreen\(\)\s*\{', content)
if match:
    idx = match.start()
    new_content = content[:idx] + '''@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun MainScreen() {
    var showSplash by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
    
    androidx.compose.animation.Crossfade(
        targetState = showSplash,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300),
        label = "SplashTransition"
    ) { isSplash ->
        if (isSplash) {
            AnimatedReceiptSplash(onSplashComplete = { showSplash = false })
        } else {
            MainScreenContent()
        }
    }
}

@Composable
fun AnimatedReceiptSplash(onSplashComplete: () -> Unit) {
    val outlineProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    val line1Progress = remember { androidx.compose.animation.core.Animatable(0f) }
    val line2Progress = remember { androidx.compose.animation.core.Animatable(0f) }
    val line3Progress = remember { androidx.compose.animation.core.Animatable(0f) }
    val line4Progress = remember { androidx.compose.animation.core.Animatable(0f) }
    val line5Progress = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        // Outline 0-350ms
        launch {
            outlineProgress.animateTo(
                1f,
                animationSpec = androidx.compose.animation.core.tween(350, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
        }
        
        // Lines with stagger
        launch {
            kotlinx.coroutines.delay(350)
            line1Progress.animateTo(1f, androidx.compose.animation.core.tween(150, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        }
        launch {
            kotlinx.coroutines.delay(420)
            line2Progress.animateTo(1f, androidx.compose.animation.core.tween(150, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        }
        launch {
            kotlinx.coroutines.delay(490)
            line3Progress.animateTo(1f, androidx.compose.animation.core.tween(150, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        }
        launch {
            kotlinx.coroutines.delay(560)
            line4Progress.animateTo(1f, androidx.compose.animation.core.tween(150, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        }
        launch {
            kotlinx.coroutines.delay(630)
            line5Progress.animateTo(1f, androidx.compose.animation.core.tween(150, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        }

        kotlinx.coroutines.delay(900)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth(0.3f).aspectRatio(1f)) {
            val scaleX = size.width / 100f
            val scaleY = size.height / 100f
            
            val outlinePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(20f * scaleX, 10f * scaleY)
                lineTo(80f * scaleX, 10f * scaleY)
                lineTo(80f * scaleX, 80f * scaleY)
                lineTo(75f * scaleX, 90f * scaleY)
                lineTo(70f * scaleX, 80f * scaleY)
                lineTo(65f * scaleX, 90f * scaleY)
                lineTo(60f * scaleX, 80f * scaleY)
                lineTo(55f * scaleX, 90f * scaleY)
                lineTo(50f * scaleX, 80f * scaleY)
                lineTo(45f * scaleX, 90f * scaleY)
                lineTo(40f * scaleX, 80f * scaleY)
                lineTo(35f * scaleX, 90f * scaleY)
                lineTo(30f * scaleX, 80f * scaleY)
                lineTo(25f * scaleX, 90f * scaleY)
                lineTo(20f * scaleX, 80f * scaleY)
                close()
            }
            
            val pm = android.graphics.PathMeasure()
            
            fun drawTrimmedPath(path: androidx.compose.ui.graphics.Path, progress: Float) {
                if (progress <= 0f) return
                pm.setPath(path.asAndroidPath(), false)
                val dst = android.graphics.Path()
                pm.getSegment(0f, pm.length * progress, dst, true)
                drawPath(
                    path = dst.asComposePath(),
                    color = androidx.compose.ui.graphics.Color.White,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 5f * scaleX,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
            
            drawTrimmedPath(outlinePath, outlineProgress.value)
            
            val lineYPositions = listOf(22f, 34f, 46f, 58f, 70f)
            val lineProgresses = listOf(line1Progress, line2Progress, line3Progress, line4Progress, line5Progress)
            
            for (i in 0 until 5) {
                val y = lineYPositions[i]
                val linePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(30f * scaleX, y * scaleY)
                    lineTo(70f * scaleX, y * scaleY)
                }
                drawTrimmedPath(linePath, lineProgresses[i].value)
            }
        }
    }
}
'''
    with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
        f.write(new_content)
    print("Replaced MainScreen successfully")
else:
    print("Could not find MainScreen()")
