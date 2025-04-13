package com.masterapp.ui.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masterapp.data.local.entity.SubTask
import com.masterapp.data.local.entity.Task
import com.masterapp.data.repository.CategoryRepository
import com.masterapp.data.repository.SubTaskRepository
import com.masterapp.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val subTaskRepository: SubTaskRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Long = savedStateHandle.get<Long>("taskId") ?: 0L
    
    private val _taskState = MutableStateFlow<TaskDetailState>(TaskDetailState.Loading)
    val taskState = _taskState.asStateFlow()
    
    val categories = categoryRepository.getAllCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val subtasks = if (taskId > 0) {
        subTaskRepository.getSubtasksForTask(taskId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    } else {
        MutableStateFlow(emptyList())
    }

    init {
        if (taskId > 0) {
            loadTask()
        } else {
            _taskState.value = TaskDetailState.Success(
                Task(
                    id = 0,
                    title = "",
                    description = "",
                    isCompleted = false,
                    priority = 0,
                    dueDate = null,
                    createdAt = Date()
                )
            )
        }
    }

    private fun loadTask() {
        viewModelScope.launch {
            _taskState.value = TaskDetailState.Loading
            try {
                taskRepository.getTaskById(taskId)?.let { task ->
                    _taskState.value = TaskDetailState.Success(task)
                } ?: run {
                    _taskState.value = TaskDetailState.Error("任务未找到")
                }
            } catch (e: Exception) {
                _taskState.value = TaskDetailState.Error(e.message ?: "加载任务失败")
            }
        }
    }

    fun saveTask(
        title: String,
        description: String,
        dueDate: Date?,
        priority: Int,
        categoryId: Long?,
        estimatedTime: Int?
    ) {
        if (title.isBlank()) {
            _taskState.value = TaskDetailState.Error("标题不能为空")
            return
        }

        viewModelScope.launch {
            try {
                val currentState = taskState.value
                if (currentState is TaskDetailState.Success) {
                    val task = currentState.task.copy(
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        priority = priority,
                        categoryId = categoryId,
                        estimatedTime = estimatedTime,
                        updatedAt = Date()
                    )
                    
                    if (task.id == 0L) {
                        val newTaskId = taskRepository.insertTask(task)
                        _taskState.value = TaskDetailState.Saved(newTaskId)
                    } else {
                        taskRepository.updateTask(task)
                        _taskState.value = TaskDetailState.Saved(task.id)
                    }
                }
            } catch (e: Exception) {
                _taskState.value = TaskDetailState.Error(e.message ?: "保存任务失败")
            }
        }
    }

    fun addSubtask(title: String) {
        if (title.isBlank() || taskId == 0L) return

        viewModelScope.launch {
            try {
                val subtaskCount = subTaskRepository.getSubtaskCountForTask(taskId)
                val subtask = SubTask(
                    taskId = taskId,
                    title = title,
                    position = subtaskCount
                )
                subTaskRepository.insertSubtask(subtask)
            } catch (e: Exception) {
                _taskState.value = TaskDetailState.Error(e.message ?: "添加子任务失败")
            }
        }
    }

    fun toggleSubtaskCompleted(subtask: SubTask) {
        viewModelScope.launch {
            try {
                val updatedSubtask = subtask.copy(
                    isCompleted = !subtask.isCompleted,
                    completedDate = if (!subtask.isCompleted) Date() else null
                )
                subTaskRepository.updateSubtask(updatedSubtask)
            } catch (e: Exception) {
                _taskState.value = TaskDetailState.Error(e.message ?: "更新子任务失败")
            }
        }
    }

    fun deleteSubtask(subtask: SubTask) {
        viewModelScope.launch {
            try {
                subTaskRepository.deleteSubtask(subtask)
            } catch (e: Exception) {
                _taskState.value = TaskDetailState.Error(e.message ?: "删除子任务失败")
            }
        }
    }

    fun deleteTask() {
        if (taskId == 0L) return

        viewModelScope.launch {
            try {
                val currentState = taskState.value
                if (currentState is TaskDetailState.Success) {
                    taskRepository.deleteTask(currentState.task)
                    _taskState.value = TaskDetailState.Deleted
                }
            } catch (e: Exception) {
                _taskState.value = TaskDetailState.Error(e.message ?: "删除任务失败")
            }
        }
    }
}

sealed class TaskDetailState {
    object Loading : TaskDetailState()
    data class Success(val task: Task) : TaskDetailState()
    data class Error(val message: String) : TaskDetailState()
    data class Saved(val taskId: Long) : TaskDetailState()
    object Deleted : TaskDetailState()
}