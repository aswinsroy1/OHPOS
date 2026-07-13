with open("app/src/main/java/com/example/util/PrinterManager.kt", "r") as f:
    content = f.read()

content = content.replace(
    "val connections = BluetoothPrintersConnections.select()\n                connections?.firstOrNull { it?.device?.address == printer.address }\n                    ?: throw Exception(\"Bluetooth printer not found\")",
    "val connection = BluetoothPrintersConnections.select(printer.address)\n                connection ?: throw Exception(\"Bluetooth printer not found\")"
)

with open("app/src/main/java/com/example/util/PrinterManager.kt", "w") as f:
    f.write(content)
