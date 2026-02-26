package xyz.ankitgrai.taskplanner.shared.response

import xyz.ankitgrai.taskplanner.shared.model.CategoryDto
import xyz.ankitgrai.taskplanner.shared.model.TaskDto
import kotlinx.serialization.Serializable

@Serializable
data class SyncPullResponse(
    val tasks: List<TaskDto>,
    val categories: List<CategoryDto>,
    val deletedTaskIds: List<String>,
    val deletedCategoryIds: List<String>,
    val serverTimestamp: String,
)
