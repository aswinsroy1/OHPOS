import re
with open("app/src/main/java/com/example/util/PrinterManager.kt", "r") as f:
    content = f.read()

# Replace getPairedBluetoothPrinters
content = re.sub(
    r"fun getPairedBluetoothPrinters.*?return.*?\}",
    """@android.annotation.SuppressLint("MissingPermission")
    fun getPairedBluetoothPrinters(context: Context): List<android.bluetooth.BluetoothDevice> {
        val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
        return adapter?.bondedDevices?.toList() ?: emptyList()
    }""",
    content,
    flags=re.DOTALL
)

# Replace BLUETOOTH connection part
bt_connection = """            "BLUETOOTH", "Bluetooth" -> {
                val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                @android.annotation.SuppressLint("MissingPermission")
                val device = adapter?.bondedDevices?.firstOrNull { it.address == printer.address }
                device ?: throw Exception("Bluetooth printer not found")
                com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection(device)
            }"""

content = re.sub(
    r'"BLUETOOTH", "Bluetooth" -> \{.*?\n            \}',
    bt_connection,
    content,
    flags=re.DOTALL
)

with open("app/src/main/java/com/example/util/PrinterManager.kt", "w") as f:
    f.write(content)
