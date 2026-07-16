import re
with open("app/src/main/java/com/example/util/PrinterManager.kt", "r") as f:
    content = f.read()

# Fix the broken function getPairedBluetoothPrinters
correct_func = """@android.annotation.SuppressLint("MissingPermission")
    fun getPairedBluetoothPrinters(context: Context): List<android.bluetooth.BluetoothDevice> {
        val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
        return adapter?.bondedDevices?.toList() ?: emptyList()
    }"""
content = re.sub(r'@android\.annotation\.SuppressLint\("MissingPermission"\)\s+fun getPairedBluetoothPrinters.*?\n\s+\}\s*\?\:\s*emptyList\(\)\n\s+\}', correct_func, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/util/PrinterManager.kt", "w") as f:
    f.write(content)
