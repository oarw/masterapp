package com.masterapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long? = null,
    val startTime: Date,
    val endTime: Date? = null,
    val duration: Int, // 分钟
    val isCompleted: Boolean = false,
    val isWorkSession: Boolean = true,
    val syncId: String = "",
    val createdAt: Date = Date()
)
