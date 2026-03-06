package xyz.ankitgrai.kaizen.data.repository

import xyz.ankitgrai.kaizen.data.remote.ApiService
import xyz.ankitgrai.kaizen.db.TaskPlannerDatabase
import xyz.ankitgrai.kaizen.shared.request.AuthRequest
import xyz.ankitgrai.kaizen.shared.response.AuthResponse

class AuthRepository(
    private val database: TaskPlannerDatabase,
    private val apiService: ApiService,
) {
    fun getStoredToken(): String? {
        return database.authTokenQueries.getToken().executeAsOneOrNull()?.token
    }

    fun getStoredUserId(): String? {
        return database.authTokenQueries.getToken().executeAsOneOrNull()?.user_id
    }

    fun getStoredUsername(): String? {
        return database.authTokenQueries.getToken().executeAsOneOrNull()?.username
    }

    fun isLoggedIn(): Boolean = getStoredToken() != null

    suspend fun register(username: String, password: String, inviteCode: String): Result<AuthResponse> {
        return try {
            val response = apiService.register(AuthRequest(username, password, inviteCode))
            database.authTokenQueries.saveToken(response.token, response.userId, response.username)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(AuthRequest(username, password))
            database.authTokenQueries.saveToken(response.token, response.userId, response.username)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        database.authTokenQueries.clearToken()
        database.taskQueries.deleteAllTasks()
        database.categoryQueries.deleteAllCategories()
        database.syncQueueQueries.clearAll()
        database.syncMetaQueries.clearLastSync()
    }
}
