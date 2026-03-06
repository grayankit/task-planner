package xyz.ankitgrai.kaizen.data.sync

import xyz.ankitgrai.kaizen.data.remote.ApiService
import xyz.ankitgrai.kaizen.data.repository.CategoryRepository
import xyz.ankitgrai.kaizen.data.repository.TaskRepository
import xyz.ankitgrai.kaizen.db.TaskPlannerDatabase
import xyz.ankitgrai.kaizen.shared.request.SyncOperation
import xyz.ankitgrai.kaizen.shared.request.SyncPushRequest
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

        // Force a full pull if local data is empty (e.g. after logout/reinstall)
        val localCategories = database.categoryQueries.getAllCategories().executeAsList()
        val hasLocalData = localCategories.isNotEmpty()
        val effectiveSince = if (hasLocalData) lastSync else null

        val response = apiService.syncPull(token, effectiveSince)

        // Apply pulled changes
        response.tasks.forEach { task -> taskRepository.upsertTask(task) }

        // Dedup categories: if server sends a category whose name matches a local one
        // with a different ID, the server version wins. Remap tasks from old ID to new ID.
        response.categories.forEach { serverCat ->
            val localDuplicate = database.categoryQueries
                .findByNameCaseInsensitive(serverCat.name)
                .executeAsOneOrNull()
            if (localDuplicate != null && localDuplicate.id != serverCat.id) {
                // Remap tasks from old local ID to server's canonical ID
                val tasksWithOldId = database.taskQueries.getTasksByCategory(localDuplicate.id).executeAsList()
                for (task in tasksWithOldId) {
                    database.taskQueries.updateTask(
                        category_id = serverCat.id,
                        title = task.title,
                        description = task.description,
                        priority = task.priority,
                        due_date = task.due_date,
                        due_time = task.due_time,
                        is_completed = task.is_completed,
                        completed_at = task.completed_at,
                        updated_at = task.updated_at,
                        id = task.id,
                    )
                }
                // Force delete the old local duplicate (even if default — server's version is canonical)
                database.categoryQueries.forceDeleteCategory(localDuplicate.id)
            }
            categoryRepository.upsertCategory(serverCat)
        }

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
        tasks: List<xyz.ankitgrai.kaizen.shared.model.TaskDto>,
        categories: List<xyz.ankitgrai.kaizen.shared.model.CategoryDto>,
    ) {
        // Full replace with server state after push
        taskRepository.replaceAllTasks(tasks)
        categoryRepository.replaceAllCategories(categories)
    }
}
