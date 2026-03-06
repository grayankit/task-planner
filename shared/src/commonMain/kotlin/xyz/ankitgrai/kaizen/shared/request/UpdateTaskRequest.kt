package xyz.ankitgrai.kaizen.shared.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTaskRequest(
    val categoryId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val priority: Int? = null,
    val dueDate: String? = null,
    val dueTime: String? = null,
    val isCompleted: Boolean? = null,
)
