package com.example.util

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.hardware.usb.UsbManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

data class DiscoveredPrinter(
    val name: String,
    val address: String,
    val type: String, // "WIFI", "USB", "BLUETOOTH"
    val extraInfo: String = ""
)

class DiscoveryHelper(private val context: Context) {
    private val _discoveredPrinters = MutableStateFlow<List<DiscoveredPrinter>>(emptyList())
    val discoveredPrinters: StateFlow<List<DiscoveredPrinter>> = _discoveredPrinters.asStateFlow()

    private val printers = mutableMapOf<String, DiscoveredPrinter>()

    private fun addPrinter(printer: DiscoveredPrinter) {
        printers[printer.address] = printer
        _discoveredPrinters.value = printers.values.toList()
    }

    // --- Wi-Fi (NSD) ---
    private var nsdManager: NsdManager? = null
    private val discoveryListeners = mutableListOf<NsdManager.DiscoveryListener>()

    fun startNetworkDiscovery() {
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        val serviceTypes = listOf("_printer._tcp.", "_pdl-datastream._tcp.", "_ipp._tcp.")
        
        serviceTypes.forEach { type ->
            val listener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(regType: String) {}
                override fun onServiceFound(service: NsdServiceInfo) {
                    nsdManager?.resolveService(service, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
                        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                            val ip = serviceInfo.host?.hostAddress ?: return
                            val port = serviceInfo.port
                            addPrinter(
                                DiscoveredPrinter(
                                    name = serviceInfo.serviceName,
                                    address = "$ip:$port",
                                    type = "WIFI",
                                    extraInfo = "Network Printer"
                                )
                            )
                        }
                    })
                }
                override fun onServiceLost(service: NsdServiceInfo) {}
                override fun onDiscoveryStopped(serviceType: String) {}
                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {}
                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
            }
            discoveryListeners.add(listener)
            try {
                nsdManager?.discoverServices(type, NsdManager.PROTOCOL_DNS_SD, listener)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopNetworkDiscovery() {
        discoveryListeners.forEach { listener ->
            try { nsdManager?.stopServiceDiscovery(listener) } catch (e: Exception) {}
        }
        discoveryListeners.clear()
    }

    // --- USB ---
    fun discoverUsbPrinters() {
        try {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            usbManager.deviceList.values.forEach { device ->
                val manufacturer = if (android.os.Build.VERSION.SDK_INT >= 21) device.manufacturerName else null
                val extraInfo = if (manufacturer != null) "Manufacturer: $manufacturer • VID: ${device.vendorId} PID: ${device.productId}" else "VID: ${device.vendorId} PID: ${device.productId}"
                addPrinter(
                    DiscoveredPrinter(
                        name = device.productName ?: "Unknown USB Printer",
                        address = device.deviceName,
                        type = "USB",
                        extraInfo = extraInfo
                    )
                )
            }
        } catch (e: Exception) {}
    }

    // --- Bluetooth ---
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    
    fun discoverBluetoothPrinters() {
        // Paired devices
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter?.bondedDevices?.forEach { device ->
                addPrinter(
                    DiscoveredPrinter(
                        name = device.name ?: "Unknown Bluetooth Device",
                        address = device.address,
                        type = "BLUETOOTH",
                        extraInfo = "Paired"
                    )
                )
            }
        }
    }
    
    
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                val name = device.name
                if (!name.isNullOrBlank()) {
                    addPrinter(
                        DiscoveredPrinter(
                            name = name,
                            address = device.address,
                            type = "BLUETOOTH",
                            extraInfo = "Nearby"
                        )
                    )
                }
            }
        }
    }

    fun startBluetoothDiscovery() {
        discoverBluetoothPrinters() // add paired first
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            try {
                bluetoothAdapter?.bluetoothLeScanner?.startScan(scanCallback)
            } catch (e: Exception) {}
        }
    }

    fun stopBluetoothDiscovery() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            try {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            } catch (e: Exception) {}
        }
    }

    fun clear() {
        printers.clear()
        _discoveredPrinters.value = emptyList()
    }
}
