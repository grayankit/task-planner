package xyz.ankitgrai.kaizen.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    val id: String = "",
    val userId: String = "",
    val categoryId: String? = null,
    val title: String,
    val description: String? = null,
    val priority: Int = Priority.MEDIUM.value,
    val dueDate: String? = null,       // ISO date: "2026-02-27"
    val dueTime: String? = null,       // ISO time: "14:30"
    val isCompleted: Boolean = false,
    val completedAt: String? = null,   // ISO instant
    val createdAt: String = "",
    val updatedAt: String = "",
)
