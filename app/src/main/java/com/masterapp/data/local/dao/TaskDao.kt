package com.masterapp.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.masterapp.data.local.entity.Task
import java.util.Date
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task): Long
    
    @Update
    suspend fun update(task: Task)
    
    @Delete
    suspend fun delete(task: Task)
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?
    
    @Query("SELECT * FROM tasks WHERE parentTaskId IS NULL ORDER BY position")
    fun getAllTopLevelTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE parentTaskId = :parentId ORDER BY position")
    fun getSubtasksForParent(parentId: Long): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE isCompleted = :isCompleted ORDER BY dueDate, priority")
    fun getTasksByCompletionStatus(isCompleted: Boolean): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate, priority")
    fun getTasksForDateRange(startDate: Date, endDate: Date): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE dueDate < :now AND isCompleted = 0 ORDER BY dueDate, priority")
    fun getOverdueTasks(now: Date): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId ORDER BY position")
    fun getTasksByCategory(categoryId: Long): Flow<List<Task>>
    
    @Query("SELECT COUNT(*) FROM tasks WHERE parentTaskId = :taskId")
    suspend fun getSubTaskCount(taskId: Long): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE parentTaskId = :taskId AND isCompleted = 1")
    suspend fun getCompletedSubTaskCount(taskId: Long): Int
    
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}
