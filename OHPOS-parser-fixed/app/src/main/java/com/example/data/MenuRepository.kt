package com.example.data

import kotlinx.coroutines.flow.Flow

class MenuRepository(private val menuDao: MenuDao) {
    val activeMenuItems: Flow<List<MenuItem>> = menuDao.getActiveMenuItems()
    val allMenuItems: Flow<List<MenuItem>> = menuDao.getAllMenuItems()

    suspend fun saveMenuItem(item: MenuItem) {
        menuDao.insertAll(listOf(item))
    }

    suspend fun deleteItem(id: Int) {
        menuDao.deleteItem(id)
    }

    suspend fun updateAvailability(id: Int, isActive: Boolean) {
        menuDao.updateAvailability(id, isActive)
    }
}
