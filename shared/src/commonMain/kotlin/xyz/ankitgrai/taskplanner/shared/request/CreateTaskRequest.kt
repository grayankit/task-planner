package xyz.ankitgrai.taskplanner.shared.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequest(
    val categoryId: String? = null,
    val title: String,
    val description: String? = null,
    val priority: Int = 3,
    val dueDate: String? = null,
    val dueTime: String? = null,
)
