package com.masterapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDate: Date? = null,
    val priority: Int = 2, // 1-高, 2-中, 3-低
    val estimatedTime: Int = 0, // 预计完成时间(分钟)
    val isCompleted: Boolean = false,
    val completedDate: Date? = null,
    val categoryId: Long? = null,
    val parentTaskId: Long? = null, // 父任务ID，用于任务分组
    val position: Int = 0, // 排序位置
    val syncId: String = "", // 用于云同步
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
