package com.masterapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "ai_providers")
data class AIProvider(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val apiKey: String,
    val apiUrl: String,
    val isDefault: Boolean = false,
    val modelName: String = "",
    val maxTokens: Int = 2048,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)