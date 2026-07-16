with open("app/src/main/java/com/example/ui/components/PinEntryDialog.kt", "r") as f:
    content = f.read()

bad_block = """    var showError by remember { mutableStateOf(false) }
    LaunchedEffect(externalError) {
        if (externalError) {
            errorMessage = "Incorrect PIN"
            shake()
        }
    }
    var errorMessage by remember { mutableStateOf("") }"""

good_block = """    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    fun shake() {
        scope.launch {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showError = true
            shakeOffset.animateTo(15f, animationSpec = tween(50, easing = LinearEasing))
            shakeOffset.animateTo(-15f, animationSpec = tween(50, easing = LinearEasing))
            shakeOffset.animateTo(15f, animationSpec = tween(50, easing = LinearEasing))
            shakeOffset.animateTo(-15f, animationSpec = tween(50, easing = LinearEasing))
            shakeOffset.animateTo(0f, animationSpec = tween(50, easing = LinearEasing))
            delay(1500)
            showError = false
        }
    }

    LaunchedEffect(externalError) {
        if (externalError) {
            errorMessage = "Incorrect PIN"
            shake()
        }
    }"""

content = content.replace(bad_block, "    var showError by remember { mutableStateOf(false) }\n    var errorMessage by remember { mutableStateOf(\"\") }")

# Because I need to find the old shake and replace
old_shake_block = """    var errorMessage by remember { mutableStateOf("") }
    
    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    fun shake() {
        scope.launch {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showError = true
            shakeOffset.animateTo(15f, animationSpec = tween(50, easing = LinearEasing))
            shakeOffset.animateTo(-15f, animationSpec = tween(50, easing = LinearEasing))
            shakeOffset.animateTo(15f, animationSpec = tween(50, easing = LinearEasing))
            shakeOffset.animateTo(-15f, animationSpec = tween(50, easing = LinearEasing))
            shakeOffset.animateTo(0f, animationSpec = tween(50, easing = LinearEasing))
            delay(1500)
            showError = false
        }
    }"""

content = content.replace(old_shake_block, good_block)
with open("app/src/main/java/com/example/ui/components/PinEntryDialog.kt", "w") as f:
    f.write(content)
