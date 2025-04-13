package com.masterapp.data.repository

import com.masterapp.data.local.dao.TaskDao
import com.masterapp.data.local.entity.Task
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    fun getAllTopLevelTasks(): Flow<List<Task>> = 
        taskDao.getAllTopLevelTasks()
    
    fun getSubtasksForParent(parentId: Long): Flow<List<Task>> = 
        taskDao.getSubtasksForParent(parentId)
    
    fun getTasksByCompletionStatus(isCompleted: Boolean): Flow<List<Task>> =
        taskDao.getTasksByCompletionStatus(isCompleted)
    
    fun getTasksForDateRange(startDate: Date, endDate: Date): Flow<List<Task>> =
        taskDao.getTasksForDateRange(startDate, endDate)
    
    fun getOverdueTasks(now: Date = Date()): Flow<List<Task>> =
        taskDao.getOverdueTasks(now)
    
    fun getTasksByCategory(categoryId: Long): Flow<List<Task>> =
        taskDao.getTasksByCategory(categoryId)
    
    suspend fun getTaskById(taskId: Long): Task? =
        taskDao.getTaskById(taskId)
    
    suspend fun insertTask(task: Task): Long =
        taskDao.insert(task)
    
    suspend fun updateTask(task: Task) =
        taskDao.update(task)
    
    suspend fun deleteTask(task: Task) =
        taskDao.delete(task)
    
    suspend fun getSubTaskCount(taskId: Long): Int =
        taskDao.getSubTaskCount(taskId)
    
    suspend fun getCompletedSubTaskCount(taskId: Long): Int =
        taskDao.getCompletedSubTaskCount(taskId)
    
    suspend fun deleteAllTasks() =
        taskDao.deleteAllTasks()
}
