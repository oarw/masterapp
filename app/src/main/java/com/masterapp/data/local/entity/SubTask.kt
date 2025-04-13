package com.masterapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "subtasks")
data class SubTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,  // 父任务ID
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val completedDate: Date? = null,
    val position: Int = 0,  // 排序位置
    val syncId: String = "",  // 用于云同步
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)