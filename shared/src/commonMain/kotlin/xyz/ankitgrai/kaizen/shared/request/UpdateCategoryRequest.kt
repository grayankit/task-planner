package xyz.ankitgrai.kaizen.shared.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCategoryRequest(
    val name: String? = null,
    val color: String? = null,
)
