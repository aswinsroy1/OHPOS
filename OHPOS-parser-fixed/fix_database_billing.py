import re

with open('app/src/main/java/com/example/ui/screens/BillingViewModel.kt', 'r') as f:
    content = f.read()

target = """class BillingViewModel(application: Application) : AndroidViewModel(application) {
    val defaultPrinter = database.printerDao().getAllPrinters().map { printers ->"""

replacement = """class BillingViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    
    val defaultPrinter = database.printerDao().getAllPrinters().map { printers ->"""

content = content.replace(target, replacement)

target2 = """    init {
        val database = AppDatabase.getDatabase(application)
        val menuDao = database.menuDao()"""

replacement2 = """    init {
        val menuDao = database.menuDao()"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/screens/BillingViewModel.kt', 'w') as f:
    f.write(content)

