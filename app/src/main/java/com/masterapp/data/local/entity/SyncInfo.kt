package com.masterapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "sync_info")
data class SyncInfo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lastSyncTime: Date? = null,
    val webDavUrl: String = "",
    val username: String = "",
    val password: String = "", // 实际应用中应该加密存储
    val autoSyncEnabled: Boolean = false,
    val syncInterval: Int = 24, // 同步间隔(小时)
    val lastSyncStatus: String = "",
    val updatedAt: Date = Date()
)