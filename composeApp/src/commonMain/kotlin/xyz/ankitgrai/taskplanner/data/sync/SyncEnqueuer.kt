package xyz.ankitgrai.taskplanner.data.sync

import xyz.ankitgrai.taskplanner.db.TaskPlannerDatabase
import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SyncEnqueuer(private val database: TaskPlannerDatabase) {
    @PublishedApi
    internal val json = Json { ignoreUnknownKeys = true }

    inline fun <reified T> enqueueCreate(entityType: String, entityId: String, entity: T) {
        enqueue(entityType, entityId, "CREATE", json.encodeToString(entity))
    }

    inline fun <reified T> enqueueUpdate(entityType: String, entityId: String, entity: T) {
        enqueue(entityType, entityId, "UPDATE", json.encodeToString(entity))
    }

    fun enqueueDelete(entityType: String, entityId: String) {
        enqueue(entityType, entityId, "DELETE", "{}")
    }

    fun enqueue(entityType: String, entityId: String, operationType: String, payload: String) {
        database.syncQueueQueries.insertOperation(
            id = uuid4().toString(),
            entity_type = entityType,
            entity_id = entityId,
            operation_type = operationType,
            payload = payload,
            timestamp = Clock.System.now().toString(),
            retry_count = 0,
        )
    }
}
