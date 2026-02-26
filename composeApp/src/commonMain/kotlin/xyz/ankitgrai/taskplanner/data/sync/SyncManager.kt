package xyz.ankitgrai.taskplanner.data.sync

import xyz.ankitgrai.taskplanner.data.remote.ApiService
import xyz.ankitgrai.taskplanner.data.repository.CategoryRepository
import xyz.ankitgrai.taskplanner.data.repository.TaskRepository
import xyz.ankitgrai.taskplanner.db.TaskPlannerDatabase
import xyz.ankitgrai.taskplanner.shared.request.SyncOperation
import xyz.ankitgrai.taskplanner.shared.request.SyncPushRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SyncManager(
    private val database: TaskPlannerDatabase,
    private val apiService: ApiService,
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
) {
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _lastSyncError = MutableStateFlow<String?>(null)
    val lastSyncError = _lastSyncError.asStateFlow()

    private var syncJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun startPeriodicSync(token: String, intervalMs: Long = 60_000) {
        syncJob?.cancel()
        syncJob = scope.launch {
            while (isActive) {
                sync(token)
                delay(intervalMs)
            }
        }
    }

    fun stopPeriodicSync() {
        syncJob?.cancel()
        syncJob = null
    }

    suspend fun sync(token: String) {
        if (_isSyncing.value) return
        _isSyncing.value = true
        _lastSyncError.value = null

        try {
            // 1. Push pending local changes
            pushLocalChanges(token)

            // 2. Pull remote changes
            pullRemoteChanges(token)
        } catch (e: Exception) {
            _lastSyncError.value = e.message ?: "Sync failed"
        } finally {
            _isSyncing.value = false
        }
    }

    private suspend fun pushLocalChanges(token: String) {
        val pendingOps = database.syncQueueQueries.getAllPending().executeAsList()
        if (pendingOps.isEmpty()) return

        val operations = pendingOps.map { op ->
            SyncOperation(
                id = op.id,
                entityType = op.entity_type,
                entityId = op.entity_id,
                operationType = op.operation_type,
                payload = op.payload,
                timestamp = op.timestamp,
            )
        }

        try {
            val response = apiService.syncPush(token, SyncPushRequest(operations))

            // Clear successfully synced operations
            pendingOps.forEach { op ->
                database.syncQueueQueries.deleteOperation(op.id)
            }

            // Update local data with server response
            applyServerState(response.tasks, response.categories)

            // Update last sync timestamp
            database.syncMetaQueries.updateLastSync(response.serverTimestamp)
        } catch (e: Exception) {
            // Increment retry count for failed operations
            pendingOps.forEach { op ->
                if (op.retry_count < 5) {
                    database.syncQueueQueries.incrementRetry(op.id)
                } else {
                    // Drop operations that have failed too many times
                    database.syncQueueQueries.deleteOperation(op.id)
                }
            }
            throw e
        }
    }

    private suspend fun pullRemoteChanges(token: String) {
        val lastSync = database.syncMetaQueries.getLastSync().executeAsOneOrNull()?.last_sync_timestamp

        val response = apiService.syncPull(token, lastSync)

        // Apply pulled changes
        response.tasks.forEach { task -> taskRepository.upsertTask(task) }
        response.categories.forEach { cat -> categoryRepository.upsertCategory(cat) }

        // Remove deleted entities
        response.deletedTaskIds.forEach { id ->
            database.taskQueries.deleteTask(id)
        }
        response.deletedCategoryIds.forEach { id ->
            database.categoryQueries.deleteCategory(id)
        }

        // Update last sync timestamp
        database.syncMetaQueries.updateLastSync(response.serverTimestamp)
    }

    private fun applyServerState(
        tasks: List<xyz.ankitgrai.taskplanner.shared.model.TaskDto>,
        categories: List<xyz.ankitgrai.taskplanner.shared.model.CategoryDto>,
    ) {
        // Full replace with server state after push
        taskRepository.replaceAllTasks(tasks)
        categoryRepository.replaceAllCategories(categories)
    }
}
