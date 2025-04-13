package com.masterapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long? = null,  // 可选关联任务
    val startTime: Date,
    val endTime: Date? = null,
    val duration: Int = 0,  // 以分钟为单位
    val isWorkSession: Boolean = true,  // true=工作时段，false=休息时段
    val completed: Boolean = false,
    val note: String = "",
    val syncId: String = "",  // 用于云同步
    val createdAt: Date = Date()
)
