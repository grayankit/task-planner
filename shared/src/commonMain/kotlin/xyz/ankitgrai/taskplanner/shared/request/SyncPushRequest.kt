package xyz.ankitgrai.taskplanner.shared.request

import kotlinx.serialization.Serializable

@Serializable
data class SyncPushRequest(
    val operations: List<SyncOperation>,
)

@Serializable
data class SyncOperation(
    val id: String,
    val entityType: String,     // "task" or "category"
    val entityId: String,
    val operationType: String,  // "CREATE", "UPDATE", "DELETE"
    val payload: String,        // JSON string of the entity
    val timestamp: String,      // ISO instant
)
