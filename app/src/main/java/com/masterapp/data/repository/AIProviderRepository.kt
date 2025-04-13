package com.masterapp.data.repository

import com.masterapp.data.local.dao.AIProviderDao
import com.masterapp.data.local.entity.AIProvider
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AIProviderRepository @Inject constructor(
    private val aiProviderDao: AIProviderDao
) {
    fun getAllProviders(): Flow<List<AIProvider>> =
        aiProviderDao.getAllProviders()
    
    suspend fun getProviderById(providerId: Long): AIProvider? =
        aiProviderDao.getProviderById(providerId)
    
    suspend fun getDefaultProvider(): AIProvider? =
        aiProviderDao.getDefaultProvider()
    
    suspend fun setDefaultProvider(provider: AIProvider) {
        aiProviderDao.clearDefaultProvider()
        aiProviderDao.update(provider.copy(isDefault = true))
    }
    
    suspend fun insertProvider(provider: AIProvider): Long =
        aiProviderDao.insert(provider)
    
    suspend fun updateProvider(provider: AIProvider) =
        aiProviderDao.update(provider)
    
    suspend fun deleteProvider(provider: AIProvider) =
        aiProviderDao.delete(provider)
    
    suspend fun deleteAllProviders() =
        aiProviderDao.deleteAllProviders()
}