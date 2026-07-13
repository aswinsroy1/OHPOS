with open("app/src/main/java/com/example/util/PrinterManager.kt", "r") as f:
    content = f.read()

content = content.replace(
    "val connection = BluetoothPrintersConnections.select(printer.address)\n                connection ?: throw Exception(\"Bluetooth printer not found\")",
    "val connections = BluetoothPrintersConnections.select()\n                val connection = connections?.firstOrNull { it?.device?.address == printer.address }\n                connection ?: throw Exception(\"Bluetooth printer not found\")"
)

with open("app/src/main/java/com/example/util/PrinterManager.kt", "w") as f:
    f.write(content)
