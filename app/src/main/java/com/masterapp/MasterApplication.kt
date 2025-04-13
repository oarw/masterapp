package com.masterapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.Room
import com.masterapp.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MasterApplication : Application() {

    // 应用级协程作用域
    val applicationScope = CoroutineScope(SupervisorJob())
    
    // 数据库单例
    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "master_database"
        ).build()
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 任务提醒通道
            val taskChannel = NotificationChannel(
                "task_channel",
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Task deadline reminders"
            }
            
            // 番茄钟通道
            val pomodoroChannel = NotificationChannel(
                "pomodoro_channel",
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pomodoro timer notifications"
            }
            
            // 应用通知通道
            val appChannel = NotificationChannel(
                "app_channel",
                "App Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "General app notifications"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannels(listOf(taskChannel, pomodoroChannel, appChannel))
        }
    }
}
