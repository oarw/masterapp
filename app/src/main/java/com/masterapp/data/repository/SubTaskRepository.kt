package com.masterapp.data.repository

import com.masterapp.data.local.dao.SubTaskDao
import com.masterapp.data.local.entity.SubTask
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubTaskRepository @Inject constructor(
    private val subTaskDao: SubTaskDao
) {
    fun getSubtasksForTask(taskId: Long): Flow<List<SubTask>> =
        subTaskDao.getSubtasksForTask(taskId)
    
    fun getSubtasksByCompletionStatus(isCompleted: Boolean): Flow<List<SubTask>> =
        subTaskDao.getSubtasksByCompletionStatus(isCompleted)
    
    suspend fun getSubtaskById(subtaskId: Long): SubTask? =
        subTaskDao.getSubtaskById(subtaskId)
    
    suspend fun insertSubtask(subtask: SubTask): Long =
        subTaskDao.insert(subtask)
    
    suspend fun updateSubtask(subtask: SubTask) =
        subTaskDao.update(subtask)
    
    suspend fun deleteSubtask(subtask: SubTask) =
        subTaskDao.delete(subtask)
    
    suspend fun getSubtaskCountForTask(taskId: Long): Int =
        subTaskDao.getSubtaskCountForTask(taskId)
    
    suspend fun getCompletedSubtaskCountForTask(taskId: Long): Int =
        subTaskDao.getCompletedSubtaskCountForTask(taskId)
    
    suspend fun deleteSubtasksForTask(taskId: Long) =
        subTaskDao.deleteSubtasksForTask(taskId)
    
    suspend fun deleteAllSubtasks() =
        subTaskDao.deleteAllSubtasks()
}