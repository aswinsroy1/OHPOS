package com.example.ui.screens

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SavedPrinter
import com.example.util.PrinterManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrinterViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)

    private val _savedPrinters = MutableStateFlow<List<SavedPrinter>>(emptyList())
    val savedPrinters: StateFlow<List<SavedPrinter>> = _savedPrinters.asStateFlow()
    
    private val _isPrinting = MutableStateFlow(false)
    val isPrinting: StateFlow<Boolean> = _isPrinting.asStateFlow()
    
    private val _printError = MutableStateFlow<String?>(null)
    val printError: StateFlow<String?> = _printError.asStateFlow()

    init {
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
    }

    
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

    fun addPrinter(name: String, type: String, address: String, paperWidth: Int, isDefault: Boolean) {
        viewModelScope.launch {
            if (isDefault) {
                database.printerDao().clearDefaultPrinters()
            }
            database.printerDao().insertPrinter(
                SavedPrinter(
                    name = name,
                    type = type,
                    address = address,
                    paperWidth = paperWidth,
                    isDefault = isDefault || _savedPrinters.value.isEmpty()
                )
            )
        }
    }

    fun deletePrinter(printer: SavedPrinter) {
        viewModelScope.launch {
            database.printerDao().deletePrinter(printer)
        }
    }

    fun setDefaultPrinter(printer: SavedPrinter) {
        viewModelScope.launch {
            database.printerDao().clearDefaultPrinters()
            database.printerDao().updatePrinter(printer.copy(isDefault = true))
        }
    }

    fun testPrint(context: Context, printer: SavedPrinter) {
        viewModelScope.launch {
            _isPrinting.value = true
            _printError.value = null
            
            val success = PrinterManager.printTest(context, printer)
            
            if (!success) {
                _printError.value = "Failed to connect to printer. Please check if it's turned on and paired/connected properly."
            }
            
            _isPrinting.value = false
        }
    }
    
    fun clearError() {
        _printError.value = null
    }
}
