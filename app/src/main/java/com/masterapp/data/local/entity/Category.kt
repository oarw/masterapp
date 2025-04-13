package com.masterapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: String,
    val icon: String? = null,
    val syncId: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
