package com.masterapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.masterapp.data.local.dao.*
import com.masterapp.data.local.entity.*
import com.masterapp.data.local.util.Converters

@Database(
    entities = [
        Schedule::class,
        Task::class, 
        SubTask::class, 
        Category::class, 
        Tag::class, 
        PomodoroSession::class,
        StatisticsEntry::class,
        AIProvider::class,
        SyncInfo::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun taskDao(): TaskDao
    abstract fun subTaskDao(): SubTaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao
    abstract fun pomodoroSessionDao(): PomodoroSessionDao
    abstract fun statisticsDao(): StatisticsDao
    abstract fun aiProviderDao(): AIProviderDao
    abstract fun syncInfoDao(): SyncInfoDao
}
