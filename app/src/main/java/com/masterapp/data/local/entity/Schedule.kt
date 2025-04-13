package com.masterapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val startTime: Date,
    val endTime: Date,
    val location: String = "",
    val categoryId: Long? = null,
    val isAllDay: Boolean = false,
    val reminderMinutesBefore: Int? = null,
    val isCompleted: Boolean = false,
    val taskId: Long? = null,  // 可选关联的任务ID
    val syncId: String = "",   // 用于云同步
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
