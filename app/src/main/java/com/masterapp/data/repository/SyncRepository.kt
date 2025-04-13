package com.masterapp.data.repository

import com.masterapp.data.local.dao.SyncInfoDao
import com.masterapp.data.local.entity.SyncInfo
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class SyncRepository @Inject constructor(
    private val syncInfoDao: SyncInfoDao
) {
    suspend fun getSyncInfo(): SyncInfo? =
        syncInfoDao.getSyncInfo()
    
    fun getSyncInfoFlow(): Flow<SyncInfo?> =
        syncInfoDao.getSyncInfoFlow()
    
    suspend fun updateSyncInfo(syncInfo: SyncInfo) {
        val existingSyncInfo = syncInfoDao.getSyncInfo()
        if (existingSyncInfo == null) {
            syncInfoDao.insert(syncInfo)
        } else {
            syncInfoDao.update(syncInfo.copy(id = existingSyncInfo.id))
        }
    }
    
    suspend fun updateSyncStatus(status: String) {
        val existingSyncInfo = syncInfoDao.getSyncInfo()
        if (existingSyncInfo == null) {
            val newSyncInfo = SyncInfo(lastSyncStatus = status)
            syncInfoDao.insert(newSyncInfo)
        } else {
            syncInfoDao.update(existingSyncInfo.copy(
                lastSyncStatus = status,
                updatedAt = Date()
            ))
        }
    }
    
    suspend fun updateLastSyncTime() {
        val existingSyncInfo = syncInfoDao.getSyncInfo()
        val now = Date()
        if (existingSyncInfo == null) {
            val newSyncInfo = SyncInfo(lastSyncTime = now)
            syncInfoDao.insert(newSyncInfo)
        } else {
            syncInfoDao.update(existingSyncInfo.copy(
                lastSyncTime = now,
                updatedAt = now
            ))
        }
    }
    
    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        val existingSyncInfo = syncInfoDao.getSyncInfo()
        if (existingSyncInfo == null) {
            val newSyncInfo = SyncInfo(autoSyncEnabled = enabled)
            syncInfoDao.insert(newSyncInfo)
        } else {
            syncInfoDao.update(existingSyncInfo.copy(
                autoSyncEnabled = enabled,
                updatedAt = Date()
            ))
        }
    }
    
    suspend fun setSyncCredentials(url: String, username: String, password: String) {
        val existingSyncInfo = syncInfoDao.getSyncInfo()
        if (existingSyncInfo == null) {
            val newSyncInfo = SyncInfo(
                webDavUrl = url,
                username = username,
                password = password
            )
            syncInfoDao.insert(newSyncInfo)
        } else {
            syncInfoDao.update(existingSyncInfo.copy(
                webDavUrl = url,
                username = username,
                password = password,
                updatedAt = Date()
            ))
        }
    }
    
    suspend fun resetSyncInfo() =
        syncInfoDao.deleteAllSyncInfo()
}