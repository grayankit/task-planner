package xyz.ankitgrai.taskplanner.data.remote

import xyz.ankitgrai.taskplanner.shared.model.CategoryDto
import xyz.ankitgrai.taskplanner.shared.model.TaskDto
import xyz.ankitgrai.taskplanner.shared.request.*
import xyz.ankitgrai.taskplanner.shared.response.AuthResponse
import xyz.ankitgrai.taskplanner.shared.response.SyncPullResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ApiService(private val client: HttpClient, private val baseUrl: String) {

    // ─── Auth ───────────────────────────────────────────────

    suspend fun register(request: AuthRequest): AuthResponse =
        client.post("$baseUrl/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun login(request: AuthRequest): AuthResponse =
        client.post("$baseUrl/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    // ─── Tasks ──────────────────────────────────────────────

    suspend fun getTasks(token: String): List<TaskDto> =
        client.get("$baseUrl/api/tasks") {
            bearerAuth(token)
        }.body()

    suspend fun getTasksByCategory(token: String, categoryId: String): List<TaskDto> =
        client.get("$baseUrl/api/tasks") {
            bearerAuth(token)
            parameter("category_id", categoryId)
        }.body()

    suspend fun createTask(token: String, request: CreateTaskRequest): TaskDto =
        client.post("$baseUrl/api/tasks") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updateTask(token: String, taskId: String, request: UpdateTaskRequest): TaskDto =
        client.put("$baseUrl/api/tasks/$taskId") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun deleteTask(token: String, taskId: String) {
        client.delete("$baseUrl/api/tasks/$taskId") {
            bearerAuth(token)
        }
    }

    // ─── Categories ─────────────────────────────────────────

    suspend fun getCategories(token: String): List<CategoryDto> =
        client.get("$baseUrl/api/categories") {
            bearerAuth(token)
        }.body()

    suspend fun createCategory(token: String, request: CreateCategoryRequest): CategoryDto =
        client.post("$baseUrl/api/categories") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updateCategory(token: String, categoryId: String, request: UpdateCategoryRequest): CategoryDto =
        client.put("$baseUrl/api/categories/$categoryId") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun deleteCategory(token: String, categoryId: String) {
        client.delete("$baseUrl/api/categories/$categoryId") {
            bearerAuth(token)
        }
    }

    // ─── Sync ───────────────────────────────────────────────

    suspend fun syncPull(token: String, since: String?): SyncPullResponse =
        client.get("$baseUrl/api/sync/pull") {
            bearerAuth(token)
            if (since != null) parameter("since", since)
        }.body()

    suspend fun syncPush(token: String, request: SyncPushRequest): SyncPullResponse =
        client.post("$baseUrl/api/sync/push") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
