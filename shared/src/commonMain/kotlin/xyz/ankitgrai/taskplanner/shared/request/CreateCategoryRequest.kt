package xyz.ankitgrai.taskplanner.shared.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val color: String? = null,
)
