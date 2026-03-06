package xyz.ankitgrai.kaizen.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String = "",
    val userId: String = "",
    val name: String,
    val color: String? = null,       // hex color e.g. "#FF5733"
    val isDefault: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = "",
)
