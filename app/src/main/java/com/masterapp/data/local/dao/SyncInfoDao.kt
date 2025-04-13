package com.masterapp.data.local.dao

import androidx.room.*
import com.masterapp.data.local.entity.SyncInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncInfoDao {
    @Insert
    suspend fun insert(syncInfo: SyncInfo): Long
    
    @Update
    suspend fun update(syncInfo: SyncInfo)
    
    @Delete
    suspend fun delete(syncInfo: SyncInfo)
    
    @Query("SELECT * FROM sync_info LIMIT 1")
    suspend fun getSyncInfo(): SyncInfo?
    
    @Query("SELECT * FROM sync_info LIMIT 1")
    fun getSyncInfoFlow(): Flow<SyncInfo?>
    
    @Query("DELETE FROM sync_info")
    suspend fun deleteAllSyncInfo()
}