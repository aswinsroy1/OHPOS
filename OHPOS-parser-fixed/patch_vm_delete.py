import re

with open("app/src/main/java/com/example/ui/screens/BillingViewModel.kt", "r") as f:
    content = f.read()

delete_func = """    fun deleteMenuItem(id: Int) {
        viewModelScope.launch {
            repository.deleteItem(id)
            _cart.update { currentCart ->
                currentCart.filter { it.menuItem.id != id }
            }
        }
    }
"""

content = content.replace("fun updateMenuAvailability(id: Int, isActive: Boolean) {", delete_func + "\n    fun updateMenuAvailability(id: Int, isActive: Boolean) {")

with open("app/src/main/java/com/example/ui/screens/BillingViewModel.kt", "w") as f:
    f.write(content)
