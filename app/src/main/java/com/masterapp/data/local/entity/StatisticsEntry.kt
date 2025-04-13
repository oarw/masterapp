package com.masterapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "statistics")
data class StatisticsEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Date,
    val completedTasksCount: Int = 0,
    val pendingTasksCount: Int = 0,
    val pomodoroSessionsCount: Int = 0,
    val totalWorkMinutes: Int = 0,
    val totalBreakMinutes: Int = 0,
    val productivityScore: Int = 0, // 0-100的评分
    val syncId: String = "",
    val createdAt: Date = Date()
)