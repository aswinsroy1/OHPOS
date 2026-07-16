import re

with open("app/src/main/java/com/example/util/PrinterManager.kt", "r") as f:
    content = f.read()

# Replace references to missing properties with blank strings or 0
content = content.replace("item.notes.isNotBlank()", "false")
content = content.replace("item.notes", '""')
content = content.replace("bill.orderType", "bill.orderMode")
content = content.replace("bill.customerName.isNotBlank()", "false")
content = content.replace("bill.customerPhone.isNotBlank()", "false")
content = content.replace("bill.taxAmount", "0.0")
content = content.replace("bill.discountAmount", "0.0")
content = content.replace("bill.paymentMethod", '"Cash"')

with open("app/src/main/java/com/example/util/PrinterManager.kt", "w") as f:
    f.write(content)
