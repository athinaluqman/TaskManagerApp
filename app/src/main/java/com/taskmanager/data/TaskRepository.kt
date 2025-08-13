package com.taskmanager.data

import com.taskmanager.model.TaskDto
import com.taskmanager.network.TaskService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository(private val service: TaskService) {

    suspend fun getTasks(listId: String = "@default", maxResults: Int = 20, pageToken: String? = null) = withContext(Dispatchers.IO) {
        val response = service.getTasks(listId, maxResults, pageToken)
        TasksPage(
            items = response.items ?: emptyList(),
            nextPageToken = response.nextPageToken
        )
    }

    suspend fun insertTask(task: TaskDto, listId: String = "@default") = withContext(Dispatchers.IO) {
        service.insertTask(listId, task)
    }

    suspend fun updateTask(taskId: String, task: TaskDto, listId: String = "@default") = withContext(Dispatchers.IO) {
        service.updateTask(listId, taskId, task)
    }

    suspend fun deleteTask(taskId: String, listId: String = "@default") = withContext(Dispatchers.IO) {
        service.deleteTask(listId, taskId)
    }

    suspend fun getTaskById(taskId: String, listId: String = "@default"): TaskDto? = withContext(Dispatchers.IO) {
        service.getTasks(listId).items?.firstOrNull { it.id == taskId }
    }
}
