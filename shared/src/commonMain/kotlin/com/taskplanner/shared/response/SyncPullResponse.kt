package com.taskplanner.shared.response

import com.taskplanner.shared.model.CategoryDto
import com.taskplanner.shared.model.TaskDto
import kotlinx.serialization.Serializable

@Serializable
data class SyncPullResponse(
    val tasks: List<TaskDto>,
    val categories: List<CategoryDto>,
    val deletedTaskIds: List<String>,
    val deletedCategoryIds: List<String>,
    val serverTimestamp: String,
)
