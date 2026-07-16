with open("app/src/main/java/com/example/ui/screens/PrinterViewModel.kt", "r") as f:
    content = f.read()

funcs = """
    private val _connectionStatuses = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    
    fun getConnectionStatus(printerId: Int): StateFlow<Boolean> {
        val flow = MutableStateFlow(_connectionStatuses.value[printerId] ?: false)
        viewModelScope.launch {
            _connectionStatuses.collect { statuses ->
                flow.value = statuses[printerId] ?: false
            }
        }
        return flow.asStateFlow()
    }

    fun connectPrinter(printer: SavedPrinter) {
        viewModelScope.launch {
            // Simulated connect delay
            kotlinx.coroutines.delay(500)
            val updated = _connectionStatuses.value.toMutableMap()
            updated[printer.id] = true
            _connectionStatuses.value = updated
        }
    }

    fun disconnectPrinter(printer: SavedPrinter) {
        viewModelScope.launch {
            val updated = _connectionStatuses.value.toMutableMap()
            updated[printer.id] = false
            _connectionStatuses.value = updated
        }
    }
"""

content = content.replace("fun addPrinter(", funcs + "\n    fun addPrinter(")

# Auto connect default printer
init_old = """    init {
        viewModelScope.launch {
            database.printerDao().getAllPrinters().collect {
                _savedPrinters.value = it
            }
        }
    }"""
init_new = """    init {
        viewModelScope.launch {
            database.printerDao().getAllPrinters().collect {
                _savedPrinters.value = it
                // auto connect default
                val def = it.firstOrNull { p -> p.isDefault } ?: it.firstOrNull()
                if (def != null && _connectionStatuses.value[def.id] != true) {
                    connectPrinter(def)
                }
            }
        }
    }"""
content = content.replace(init_old, init_new)

with open("app/src/main/java/com/example/ui/screens/PrinterViewModel.kt", "w") as f:
    f.write(content)
