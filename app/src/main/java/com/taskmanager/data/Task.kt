package com.taskmanager.data

import com.taskmanager.model.TaskDto
import com.taskmanager.screen.TaskFilter
import com.taskmanager.util.TaskStatus

data class TaskUiState(
    val tasks: List<TaskDto> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val isLoading: Boolean = false,
    val selectedTask: TaskDto? = null
) {
    val filteredTasks: List<TaskDto>
        get() = when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.COMPLETED -> tasks.filter { it.status == TaskStatus.COMPLETED }
            TaskFilter.PENDING -> tasks.filter { it.status == TaskStatus.NEEDS_ACTION }
        }
}

data class TasksPage(
    val items: List<TaskDto>,
    val nextPageToken: String? = null
)