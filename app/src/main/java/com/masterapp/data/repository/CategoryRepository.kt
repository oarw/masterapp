package com.masterapp.data.repository

import com.masterapp.data.local.dao.CategoryDao
import com.masterapp.data.local.entity.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories()
    
    fun searchCategories(searchQuery: String): Flow<List<Category>> =
        categoryDao.searchCategories("%$searchQuery%")
    
    suspend fun getCategoryById(categoryId: Long): Category? =
        categoryDao.getCategoryById(categoryId)
    
    suspend fun insertCategory(category: Category): Long =
        categoryDao.insert(category)
    
    suspend fun updateCategory(category: Category) =
        categoryDao.update(category)
    
    suspend fun deleteCategory(category: Category) =
        categoryDao.delete(category)
    
    suspend fun deleteAllCategories() =
        categoryDao.deleteAllCategories()
}