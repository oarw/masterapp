package com.masterapp.data.local.dao

import androidx.room.*
import com.masterapp.data.local.entity.SubTask
import kotlinx.coroutines.flow.Flow

@Dao
interface SubTaskDao {
    @Insert
    suspend fun insert(subtask: SubTask): Long
    
    @Update
    suspend fun update(subtask: SubTask)
    
    @Delete
    suspend fun delete(subtask: SubTask)
    
    @Query("SELECT * FROM subtasks WHERE id = :subtaskId")
    suspend fun getSubtaskById(subtaskId: Long): SubTask?
    
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY position")
    fun getSubtasksForTask(taskId: Long): Flow<List<SubTask>>
    
    @Query("SELECT * FROM subtasks WHERE isCompleted = :isCompleted ORDER BY position")
    fun getSubtasksByCompletionStatus(isCompleted: Boolean): Flow<List<SubTask>>
    
    @Query("SELECT COUNT(*) FROM subtasks WHERE taskId = :taskId")
    suspend fun getSubtaskCountForTask(taskId: Long): Int
    
    @Query("SELECT COUNT(*) FROM subtasks WHERE taskId = :taskId AND isCompleted = 1")
    suspend fun getCompletedSubtaskCountForTask(taskId: Long): Int
    
    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasksForTask(taskId: Long)
    
    @Query("DELETE FROM subtasks")
    suspend fun deleteAllSubtasks()
}