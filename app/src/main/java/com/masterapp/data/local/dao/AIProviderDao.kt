package com.masterapp.data.local.dao

import androidx.room.*
import com.masterapp.data.local.entity.AIProvider
import kotlinx.coroutines.flow.Flow

@Dao
interface AIProviderDao {
    @Insert
    suspend fun insert(aiProvider: AIProvider): Long
    
    @Update
    suspend fun update(aiProvider: AIProvider)
    
    @Delete
    suspend fun delete(aiProvider: AIProvider)
    
    @Query("SELECT * FROM ai_providers WHERE id = :providerId")
    suspend fun getProviderById(providerId: Long): AIProvider?
    
    @Query("SELECT * FROM ai_providers ORDER BY name")
    fun getAllProviders(): Flow<List<AIProvider>>
    
    @Query("SELECT * FROM ai_providers WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultProvider(): AIProvider?
    
    @Query("UPDATE ai_providers SET isDefault = 0")
    suspend fun clearDefaultProvider()
    
    @Query("DELETE FROM ai_providers")
    suspend fun deleteAllProviders()
}