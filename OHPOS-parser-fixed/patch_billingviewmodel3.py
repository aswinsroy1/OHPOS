import re

with open('app/src/main/java/com/example/ui/screens/BillingViewModel.kt', 'r') as f:
    content = f.read()

new_props = """
    val defaultPrinter = database.printerDao().getAllPrinters().map { printers ->
        printers.firstOrNull { it.isDefault } ?: printers.firstOrNull()
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), null)

"""

if "val defaultPrinter =" not in content[:content.find("init {")]:
    content = content.replace('class BillingViewModel(application: Application) : AndroidViewModel(application) {', 
                              'class BillingViewModel(application: Application) : AndroidViewModel(application) {' + new_props)

with open('app/src/main/java/com/example/ui/screens/BillingViewModel.kt', 'w') as f:
    f.write(content)

