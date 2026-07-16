import re
with open("app/src/main/java/com/example/util/PrinterManager.kt", "r") as f:
    content = f.read()

# Add SimpleDeviceInfo
content = content.replace("object PrinterManager {", "data class SimpleDeviceInfo(val name: String, val address: String)\n\nobject PrinterManager {")

# Replace getPairedBluetoothPrinters
replacement = """@android.annotation.SuppressLint("MissingPermission")
    fun getPairedBluetoothPrinters(context: Context): List<SimpleDeviceInfo> {
        val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
        return adapter?.bondedDevices?.map { SimpleDeviceInfo(it.name ?: "Unknown", it.address) } ?: emptyList()
    }"""
content = re.sub(
    r'@android\.annotation\.SuppressLint\("MissingPermission"\)\s+fun getPairedBluetoothPrinters.*?return.*?\}',
    replacement,
    content,
    flags=re.DOTALL
)

with open("app/src/main/java/com/example/util/PrinterManager.kt", "w") as f:
    f.write(content)
