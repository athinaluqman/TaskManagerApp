package com.taskmanager.model

import com.google.gson.annotations.SerializedName

data class TaskListsResponse(
    val items: List<TaskListDto>?
)

data class TaskListDto(
    val id: String?,
    val title: String?
)

data class TasksResponse(
    val items: List<TaskDto>?,
    val nextPageToken: String? = null
)

data class TaskDto(
    val id: String? = null,
    val title: String? = null,
    @SerializedName("notes")
    val description: String? = null,
    val status: String? = null,
    @SerializedName("due")
    val dueDate: String? = null
)
