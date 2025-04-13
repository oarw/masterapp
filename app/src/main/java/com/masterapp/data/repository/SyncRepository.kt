package com.masterapp.data.repository

import android.content.Context
import com.github.sardine.SardineFactory
import com.masterapp.data.local.dao.SyncInfoDao
import com.masterapp.data.local.entity.SyncInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val syncInfoDao: SyncInfoDao,
    private val context: Context
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
    
    suspend fun setAutoSync(enabled: Boolean) {
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
    
    suspend fun saveSyncCredentials(url: String, username: String, password: String) {
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
        
    suspend fun testConnection(url: String, username: String, password: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val sardine = SardineFactory.begin(username, password)
                sardine.exists(url)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun isAutoSyncEnabled(): Boolean {
        val syncInfo = syncInfoDao.getSyncInfo() ?: return false
        return syncInfo.autoSyncEnabled
    }
    
    suspend fun syncToWebdav(): Boolean {
        val syncInfo = syncInfoDao.getSyncInfo() ?: return false
        if (syncInfo.webDavUrl.isNullOrEmpty() || syncInfo.username.isNullOrEmpty() || syncInfo.password.isNullOrEmpty()) {
            return false
        }
        
        return try {
            withContext(Dispatchers.IO) {
                val databasePath = context.getDatabasePath("master_app_database").absolutePath
                val databaseFile = File(databasePath)
                val backupFile = File(context.cacheDir, "master_app_backup.zip")
                
                // 创建备份文件
                FileOutputStream(backupFile).use { fileOut ->
                    val zipOut = java.util.zip.ZipOutputStream(fileOut)
                    
                    // 添加数据库文件
                    val entry = java.util.zip.ZipEntry(databaseFile.name)
                    zipOut.putNextEntry(entry)
                    FileInputStream(databaseFile).use { fileIn ->
                        fileIn.copyTo(zipOut)
                    }
                    zipOut.closeEntry()
                    
                    // 添加共享首选项
                    val sharedPrefsDir = File(context.dataDir, "shared_prefs")
                    if (sharedPrefsDir.exists() && sharedPrefsDir.isDirectory) {
                        sharedPrefsDir.listFiles()?.forEach { file ->
                            val prefsEntry = java.util.zip.ZipEntry("shared_prefs/${file.name}")
                            zipOut.putNextEntry(prefsEntry)
                            FileInputStream(file).use { fileIn ->
                                fileIn.copyTo(zipOut)
                            }
                            zipOut.closeEntry()
                        }
                    }
                    
                    zipOut.close()
                }
                
                // 上传到WebDAV
                val sardine = SardineFactory.begin(syncInfo.username, syncInfo.password)
                val webdavUrl = syncInfo.webDavUrl.let {
                    if (it.endsWith("/")) it else "$it/"
                }
                val remotePath = "${webdavUrl}master_app_backup.zip"
                
                // 如果远程目录不存在，创建它
                if (!sardine.exists(webdavUrl)) {
                    sardine.createDirectory(webdavUrl)
                }
                
                // 上传备份文件
                sardine.put(remotePath, backupFile, "application/zip")
                
                // 更新同步时间
                updateLastSyncTime()
                updateSyncStatus("成功")
                
                // 清理
                backupFile.delete()
                
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            updateSyncStatus("失败: ${e.message}")
            false
        }
    }
    
    suspend fun restoreFromWebdav(): Boolean {
        val syncInfo = syncInfoDao.getSyncInfo() ?: return false
        if (syncInfo.webDavUrl.isNullOrEmpty() || syncInfo.username.isNullOrEmpty() || syncInfo.password.isNullOrEmpty()) {
            return false
        }
        
        return try {
            withContext(Dispatchers.IO) {
                val databasePath = context.getDatabasePath("master_app_database").absolutePath
                val databaseFile = File(databasePath)
                val databaseTempFile = File("${databasePath}.tmp")
                val backupFile = File(context.cacheDir, "master_app_backup.zip")
                
                // 连接到WebDAV并下载文件
                val sardine = SardineFactory.begin(syncInfo.username, syncInfo.password)
                val webdavUrl = syncInfo.webDavUrl.let {
                    if (it.endsWith("/")) it else "$it/"
                }
                val remotePath = "${webdavUrl}master_app_backup.zip"
                
                if (!sardine.exists(remotePath)) {
                    return@withContext false
                }
                
                // 下载备份文件
                FileOutputStream(backupFile).use { outStream ->
                    sardine.get(remotePath).use { inStream ->
                        inStream.copyTo(outStream)
                    }
                }
                
                // 解压备份文件
                val sharedPrefsDir = File(context.dataDir, "shared_prefs")
                
                java.util.zip.ZipInputStream(FileInputStream(backupFile)).use { zipIn ->
                    var zipEntry = zipIn.nextEntry
                    while (zipEntry != null) {
                        val fileName = zipEntry.name
                        val targetFile = when {
                            fileName.startsWith("shared_prefs/") -> {
                                val name = fileName.substringAfter("shared_prefs/")
                                File(sharedPrefsDir, name)
                            }
                            fileName == "master_app_database" -> databaseTempFile
                            else -> null
                        }
                        
                        if (targetFile != null) {
                            targetFile.parentFile?.mkdirs()
                            FileOutputStream(targetFile).use { fileOut ->
                                zipIn.copyTo(fileOut)
                            }
                        }
                        
                        zipIn.closeEntry()
                        zipEntry = zipIn.nextEntry
                    }
                }
                
                // 替换数据库文件
                if (databaseTempFile.exists() && databaseTempFile.length() > 0) {
                    if (databaseFile.exists()) {
                        databaseFile.delete()
                    }
                    databaseTempFile.renameTo(databaseFile)
                }
                
                // 清理
                backupFile.delete()
                
                // 更新同步状态
                updateSyncStatus("恢复成功")
                
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            updateSyncStatus("恢复失败: ${e.message}")
            false
        }
    }
}