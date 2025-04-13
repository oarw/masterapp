package com.masterapp.data.local.dao

import androidx.room.*
import com.masterapp.data.local.entity.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert
    suspend fun insert(tag: Tag): Long
    
    @Update
    suspend fun update(tag: Tag)
    
    @Delete
    suspend fun delete(tag: Tag)
    
    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: Long): Tag?
    
    @Query("SELECT * FROM tags ORDER BY name")
    fun getAllTags(): Flow<List<Tag>>
    
    @Query("SELECT * FROM tags WHERE name LIKE :searchQuery ORDER BY name")
    fun searchTags(searchQuery: String): Flow<List<Tag>>
    
    @Query("DELETE FROM tags")
    suspend fun deleteAllTags()
}