package xyz.ankitgrai.kaizen.shared.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val color: String? = null,
)
