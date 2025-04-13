package com.masterapp.data.local.dao

import androidx.room.*
import com.masterapp.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category): Long
    
    @Update
    suspend fun update(category: Category)
    
    @Delete
    suspend fun delete(category: Category)
    
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?
    
    @Query("SELECT * FROM categories ORDER BY name")
    fun getAllCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE name LIKE :searchQuery ORDER BY name")
    fun searchCategories(searchQuery: String): Flow<List<Category>>
    
    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}