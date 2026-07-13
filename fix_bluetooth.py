with open("app/src/main/java/com/example/util/DiscoveryHelper.kt", "r") as f:
    content = f.read()

import re

# Add scanCallback
scan_code = """
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
"""

content = content.replace("fun clear() {", scan_code + "\n    fun clear() {")

with open("app/src/main/java/com/example/util/DiscoveryHelper.kt", "w") as f:
    f.write(content)
