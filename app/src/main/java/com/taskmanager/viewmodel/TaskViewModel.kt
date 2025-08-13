package com.taskmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskmanager.data.TaskRepository
import com.taskmanager.data.TaskUiState
import com.taskmanager.model.TaskDto
import com.taskmanager.screen.TaskFilter
import com.taskmanager.util.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState

    // Update selected task
    private val _selectedTask = MutableStateFlow<TaskDto?>(null)
    val selectedTask: StateFlow<TaskDto?> = _selectedTask.asStateFlow()

    // Pagination
    private var nextPageToken: String? = null
    private var isLoadingMore = false

    fun loadTasks(reset: Boolean = false) {
        viewModelScope.launch {
            if (isLoadingMore) return@launch
            isLoadingMore = true
            _uiState.update { it.copy(isLoading = true) }

            if (reset) {
                nextPageToken = null
                _uiState.update { it.copy(tasks = emptyList()) }
            }

            try {
                val page = repository.getTasks(pageToken = nextPageToken)
                nextPageToken = page.nextPageToken

                _uiState.update { state ->
                    val newTasks = page.items ?: emptyList()
                    state.copy(
                        tasks = if (reset) newTasks else state.tasks + newTasks,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // handle error if needed
                _uiState.update { it.copy(isLoading = false) }
            } finally {
                isLoadingMore = false
            }
        }
    }

    fun setFilter(filter: TaskFilter) { _uiState.update { it.copy(filter = filter) } }

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            val dto = repository.getTaskById(taskId)
            _selectedTask.value = dto
            _uiState.update { it.copy(selectedTask = dto) }
        }
    }

    fun saveTask(taskId: String?, title: String, description: String?, dueDate: LocalDate?) {
        viewModelScope.launch {
            val isoDue: String? = dueDate
                ?.atStartOfDay(ZoneId.systemDefault())
                ?.withZoneSameInstant(ZoneOffset.UTC)
                ?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
            val dto = TaskDto(title = title, description = description, dueDate = isoDue, status = TaskStatus.NEEDS_ACTION)
            if (taskId.isNullOrBlank()) {
                // New task
                repository.insertTask(dto)
            } else {
                // Update existing task
                repository.updateTask(taskId, dto)
            }
            loadTasks(true)
            _selectedTask.value = dto
            _uiState.update { it.copy(selectedTask = dto) }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            _selectedTask.value = null
            _uiState.update { it.copy(selectedTask = null) }
            loadTasks(true)
        }
    }

    fun toggleTaskStatus(task: TaskDto) {
        viewModelScope.launch {
            val newStatus = if (task.status == TaskStatus.COMPLETED) TaskStatus.NEEDS_ACTION else TaskStatus.COMPLETED
            val dto = task.copy(status = newStatus)
            dto.id?.let { repository.updateTask(it, dto) }
            _selectedTask.value = dto
            _uiState.update { it.copy(selectedTask = dto) }
            loadTasks(true)
        }
    }

    fun loadMoreTasks() {
        // Only load more if:
        // 1. We have a nextPageToken
        // 2. No ongoing load
        if (nextPageToken != null && !isLoadingMore) {
            loadTasks(reset = false)
        }
    }

    fun clearSelectedTask() {
        _selectedTask.value = null
        _uiState.update { it.copy(selectedTask = null) }
    }
}
