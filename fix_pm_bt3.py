with open("app/src/main/java/com/example/util/PrinterManager.kt", "r") as f:
    content = f.read()

func = """
    fun getPairedBluetoothPrinters(context: Context): List<android.bluetooth.BluetoothDevice> {
        val connections = BluetoothPrintersConnections.select()
        return connections?.mapNotNull { it?.device } ?: emptyList()
    }
"""
content = content.replace("object PrinterManager {", "object PrinterManager {" + func)

with open("app/src/main/java/com/example/util/PrinterManager.kt", "w") as f:
    f.write(content)
