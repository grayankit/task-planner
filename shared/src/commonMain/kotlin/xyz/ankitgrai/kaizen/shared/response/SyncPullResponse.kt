package xyz.ankitgrai.kaizen.shared.response

import xyz.ankitgrai.kaizen.shared.model.CategoryDto
import xyz.ankitgrai.kaizen.shared.model.TaskDto
import kotlinx.serialization.Serializable

@Serializable
data class SyncPullResponse(
    val tasks: List<TaskDto>,
    val categories: List<CategoryDto>,
    val deletedTaskIds: List<String>,
    val deletedCategoryIds: List<String>,
    val serverTimestamp: String,
)
