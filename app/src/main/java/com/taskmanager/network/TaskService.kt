package com.taskmanager.network

import com.taskmanager.model.TaskDto
import com.taskmanager.model.TaskListsResponse
import com.taskmanager.model.TasksResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskService {
    @GET("tasks/v1/users/@me/lists")
    suspend fun getTaskLists(): TaskListsResponse

    @GET("tasks/v1/lists/{listId}/tasks")
    suspend fun getTasks(
        @Path("listId") listId: String = "@default",
        @Query("maxResults") maxResults: Int? = null,   // number of tasks per page
        @Query("pageToken") pageToken: String? = null  // token for next page
    ): TasksResponse

    @POST("tasks/v1/lists/{listId}/tasks")
    suspend fun insertTask(@Path("listId") listId: String = "@default", @Body body: TaskDto): TaskDto

    @PATCH("tasks/v1/lists/{listId}/tasks/{taskId}")
    suspend fun updateTask(@Path("listId") listId: String = "@default", @Path("taskId") taskId: String, @Body body: TaskDto): TaskDto

    @DELETE("tasks/v1/lists/{listId}/tasks/{taskId}")
    suspend fun deleteTask(@Path("listId") listId: String = "@default", @Path("taskId") taskId: String)
}