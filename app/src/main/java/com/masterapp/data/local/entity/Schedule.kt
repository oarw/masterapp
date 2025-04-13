package com.masterapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.util.Date

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val startTime: Date,
    val endTime: Date,
    val location: String = "",
    val priority: Int = 2, // 1-高, 2-中, 3-低
    val reminder: Date? = null,
    val isCompleted: Boolean = false,
    val categoryId: Long? = null,
    val syncId: String = "", // 用于云同步
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
