package com.masterapp.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masterapp.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _webdavStatus = MutableStateFlow<WebdavStatus>(WebdavStatus.NotConfigured)
    val webdavStatus: StateFlow<WebdavStatus> = _webdavStatus.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _backupStatus = MutableStateFlow<BackupStatus>(BackupStatus.Idle)
    val backupStatus: StateFlow<BackupStatus> = _backupStatus.asStateFlow()

    init {
        loadWebdavSettings()
    }

    private fun loadWebdavSettings() {
        viewModelScope.launch {
            val info = syncRepository.getSyncInfo()
            _webdavStatus.value = if (info != null && !info.webdavUrl.isNullOrEmpty()) {
                WebdavStatus.Configured(info.webdavUrl)
            } else {
                WebdavStatus.NotConfigured
            }
        }
    }

    fun saveWebdavSettings(url: String, username: String, password: String) {
        viewModelScope.launch {
            syncRepository.saveSyncCredentials(url, username, password)
            _webdavStatus.value = WebdavStatus.Configured(url)
        }
    }

    fun testWebdavConnection(url: String, username: String, password: String) {
        viewModelScope.launch {
            _webdavStatus.value = WebdavStatus.Testing
            val success = syncRepository.testConnection(url, username, password)
            _webdavStatus.value = if (success) {
                WebdavStatus.ConnectionSuccess(url)
            } else {
                WebdavStatus.ConnectionFailed(url)
            }
        }
    }

    fun syncToWebdav() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            val success = syncRepository.syncToWebdav()
            _syncStatus.value = if (success) {
                SyncStatus.Success
            } else {
                SyncStatus.Failed("同步失败，请检查网络连接和WebDAV配置")
            }
        }
    }

    fun restoreFromWebdav() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Syncing
            val success = syncRepository.restoreFromWebdav()
            _syncStatus.value = if (success) {
                SyncStatus.Success
            } else {
                SyncStatus.Failed("从WebDAV恢复失败，请检查网络连接和WebDAV配置")
            }
        }
    }

    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            syncRepository.setAutoSync(enabled)
        }
    }

    fun isAutoSyncEnabled(): Boolean {
        return syncRepository.isAutoSyncEnabled()
    }

    fun backupToLocal(context: Context, uri: Uri) {
        viewModelScope.launch {
            _backupStatus.value = BackupStatus.Processing
            try {
                val result = withContext(Dispatchers.IO) {
                    val databasePath = context.getDatabasePath("master_app_database").absolutePath
                    val databaseFile = File(databasePath)
                    val databaseSharedPrefsDir = File(context.dataDir, "shared_prefs")

                    if (!databaseFile.exists()) {
                        return@withContext false
                    }

                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        ZipOutputStream(outputStream).use { zipOut ->
                            // 添加数据库文件
                            addFileToZip(databaseFile, "", zipOut)

                            // 添加共享首选项文件
                            if (databaseSharedPrefsDir.exists() && databaseSharedPrefsDir.isDirectory) {
                                databaseSharedPrefsDir.listFiles()?.forEach { file ->
                                    addFileToZip(file, "shared_prefs/", zipOut)
                                }
                            }
                        }
                    }
                    true
                }

                _backupStatus.value = if (result) {
                    BackupStatus.Success("备份成功")
                } else {
                    BackupStatus.Failed("备份失败")
                }
            } catch (e: Exception) {
                _backupStatus.value = BackupStatus.Failed("备份时发生错误: ${e.message}")
            }
        }
    }

    private fun addFileToZip(file: File, path: String, zipOut: ZipOutputStream) {
        val entry = ZipEntry(path + file.name)
        zipOut.putNextEntry(entry)

        FileInputStream(file).use { fileIn ->
            fileIn.copyTo(zipOut)
        }

        zipOut.closeEntry()
    }

    fun restoreFromLocal(context: Context, uri: Uri) {
        viewModelScope.launch {
            _backupStatus.value = BackupStatus.Processing
            try {
                val result = withContext(Dispatchers.IO) {
                    val databasePath = context.getDatabasePath("master_app_database").absolutePath
                    val databaseFile = File(databasePath)
                    val databaseTempFile = File("${databasePath}.tmp")
                    val sharedPrefsDir = File(context.dataDir, "shared_prefs")

                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        ZipInputStream(inputStream).use { zipIn ->
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
                    }

                    // 恢复完毕后，替换数据库文件
                    if (databaseTempFile.exists() && databaseTempFile.length() > 0) {
                        if (databaseFile.exists()) {
                            databaseFile.delete()
                        }
                        databaseTempFile.renameTo(databaseFile)
                    }

                    true
                }

                _backupStatus.value = if (result) {
                    BackupStatus.Success("恢复成功，请重启应用以应用更改")
                } else {
                    BackupStatus.Failed("恢复失败")
                }
            } catch (e: Exception) {
                _backupStatus.value = BackupStatus.Failed("恢复时发生错误: ${e.message}")
            }
        }
    }

    fun getCurrentDateTimeFormatted(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return sdf.format(Date())
    }
}

sealed class WebdavStatus {
    object NotConfigured : WebdavStatus()
    object Testing : WebdavStatus()
    data class Configured(val url: String) : WebdavStatus()
    data class ConnectionSuccess(val url: String) : WebdavStatus()
    data class ConnectionFailed(val url: String) : WebdavStatus()
}

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    object Success : SyncStatus()
    data class Failed(val message: String) : SyncStatus()
}

sealed class BackupStatus {
    object Idle : BackupStatus()
    object Processing : BackupStatus()
    data class Success(val message: String) : BackupStatus()
    data class Failed(val message: String) : BackupStatus()
}