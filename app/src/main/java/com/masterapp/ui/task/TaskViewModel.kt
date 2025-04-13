package com.masterapp.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masterapp.data.local.entity.Task
import com.masterapp.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _taskFilter = MutableStateFlow(TaskFilter.ALL)
    private val _taskSortOrder = MutableStateFlow(TaskSortOrder.DATE)
    private val _searchQuery = MutableStateFlow("")

    val tasks: StateFlow<List<Task>> = combine(
        _taskFilter,
        _taskSortOrder,
        _searchQuery
    ) { filter, sortOrder, query ->
        Triple(filter, sortOrder, query)
    }.flatMapLatest { (filter, sortOrder, query) ->
        when (filter) {
            TaskFilter.ALL -> if (query.isBlank()) taskRepository.getAllTasks() else taskRepository.searchTasks(query)
            TaskFilter.PENDING -> taskRepository.getTasksByCompletionStatus(false)
            TaskFilter.COMPLETED -> taskRepository.getTasksByCompletionStatus(true)
            TaskFilter.TODAY -> getTodayTasks()
            TaskFilter.IMPORTANT -> taskRepository.getImportantTasks()
        }.combine(MutableStateFlow(sortOrder)) { tasks, order ->
            when (order) {
                TaskSortOrder.DATE -> tasks.sortedByDescending { it.dueDate ?: Date(Long.MAX_VALUE) }
                TaskSortOrder.PRIORITY -> tasks.sortedByDescending { it.priority }
                TaskSortOrder.ALPHABETICAL -> tasks.sortedBy { it.title.lowercase() }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun getTodayTasks() = taskRepository.getTasksWithinDateRange(
        startDate = getTodayStart(),
        endDate = getTodayEnd()
    )

    private fun getTodayStart(): Date {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.time
    }

    private fun getTodayEnd(): Date {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.time
    }

    fun setTaskFilter(filter: TaskFilter) {
        _taskFilter.value = filter
    }

    fun setSortOrder(sortOrder: TaskSortOrder) {
        _taskSortOrder.value = sortOrder
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                isCompleted = !task.isCompleted,
                completedDate = if (!task.isCompleted) Date() else null
            )
            taskRepository.updateTask(updatedTask)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }
}