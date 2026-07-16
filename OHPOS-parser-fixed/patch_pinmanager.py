with open("app/src/main/java/com/example/util/PinManager.kt", "r") as f:
    content = f.read()

content = content.replace(
"""    fun verifyPin(pin: String): Boolean {""",
"""    fun clearPin() {
        prefs.edit().remove("pin_hash").apply()
    }

    fun verifyPin(pin: String): Boolean {"""
)

with open("app/src/main/java/com/example/util/PinManager.kt", "w") as f:
    f.write(content)
