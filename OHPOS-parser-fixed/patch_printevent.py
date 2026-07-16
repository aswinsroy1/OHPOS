import re

with open('app/src/main/java/com/example/ui/screens/BillingViewModel.kt', 'r') as f:
    content = f.read()

old_event = """sealed class PrintEvent {
    object None : PrintEvent()
    data class Success(val billId: Int) : PrintEvent()
    data class NoPrinter(val billId: Int) : PrintEvent()
    data class Failed(val billId: Int) : PrintEvent()
}"""

new_event = """sealed class PrintEvent {
    object None : PrintEvent()
    object Printing : PrintEvent()
    data class Success(val billId: Int) : PrintEvent()
    data class NoPrinter(val billId: Int) : PrintEvent()
    data class Failed(val billId: Int, val message: String = "") : PrintEvent()
    data class Offline(val billId: Int) : PrintEvent()
}"""

if old_event in content:
    content = content.replace(old_event, new_event)
else:
    print("Could not find old event")

with open('app/src/main/java/com/example/ui/screens/BillingViewModel.kt', 'w') as f:
    f.write(content)
